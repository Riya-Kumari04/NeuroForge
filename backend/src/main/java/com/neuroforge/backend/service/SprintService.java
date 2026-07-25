package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.BurndownPointResponse;
import com.neuroforge.backend.dto.CreateSprintRequest;
import com.neuroforge.backend.dto.SprintProgressResponse;
import com.neuroforge.backend.dto.SprintResponse;
import com.neuroforge.backend.dto.SprintStatisticsResponse;
import com.neuroforge.backend.dto.SprintSummaryResponse;
import com.neuroforge.backend.dto.SprintVelocityResponse;
import com.neuroforge.backend.dto.TaskDistributionResponse;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.enums.TaskPriority;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.InvalidSprintStateException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintRepository sprintRepository;
    private final TeamRepository teamRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public SprintResponse createSprint(CreateSprintRequest request) {
        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + request.getTeamId()));
        }

        SprintStatus status = request.getStatus() != null ? request.getStatus() : SprintStatus.PLANNED;

        Sprint sprint = Sprint.builder()
                .name(request.getName())
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(status)
                .team(team)
                .build();

        Sprint saved = sprintRepository.save(sprint);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> getAllSprints() {
        return sprintRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SprintResponse getSprintById(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + id));
        return mapToResponse(sprint);
    }

    @Transactional
    public SprintResponse updateSprint(UUID id, CreateSprintRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + id));

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + request.getTeamId()));
            sprint.setTeam(team);
        } else {
            sprint.setTeam(null);
        }

        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());

        Sprint updated = sprintRepository.save(sprint);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteSprint(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + id));
        sprintRepository.delete(sprint);
    }

    @Transactional
    public SprintResponse startSprint(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + id));

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new InvalidSprintStateException(
                    "Only PLANNED sprint may start. Current status: " + sprint.getStatus());
        }

        if (sprint.getTeam() != null) {
            if (sprintRepository.existsByTeamIdAndStatus(sprint.getTeam().getId(), SprintStatus.ACTIVE)) {
                throw new InvalidSprintStateException("An active sprint already exists for this team.");
            }
        } else {
            if (sprintRepository.existsByStatus(SprintStatus.ACTIVE)) {
                throw new InvalidSprintStateException("An active sprint already exists.");
            }
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setActualStartDate(LocalDate.now());

        Sprint saved = sprintRepository.save(sprint);
        return mapToResponse(saved);
    }

    @Transactional
    public SprintResponse completeSprint(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + id));

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new InvalidSprintStateException(
                    "Only ACTIVE sprint may complete. Current status: " + sprint.getStatus());
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setActualEndDate(LocalDate.now());

        Sprint saved = sprintRepository.save(sprint);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SprintResponse getActiveSprint() {
        Sprint sprint = sprintRepository.findFirstByStatus(SprintStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active sprint found"));
        return mapToResponse(sprint);
    }

    @Transactional(readOnly = true)
    public SprintSummaryResponse getSprintSummary(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + id));

        List<Task> tasks = taskRepository.findBySprintId(id);

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();
        long remainingTasks = totalTasks - completedTasks;

        double completionPercentage = totalTasks == 0 ? 0.0
                : Math.round(((double) completedTasks / totalTasks * 100.0) * 100.0) / 100.0;

        int totalStoryPoints = tasks.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
        int completedStoryPoints = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
        int remainingStoryPoints = totalStoryPoints - completedStoryPoints;

        return SprintSummaryResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .status(sprint.getStatus())
                .goal(sprint.getGoal())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .actualStartDate(sprint.getActualStartDate())
                .actualEndDate(sprint.getActualEndDate())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .remainingTasks(remainingTasks)
                .completionPercentage(completionPercentage)
                .totalStoryPoints(totalStoryPoints)
                .completedStoryPoints(completedStoryPoints)
                .remainingStoryPoints(remainingStoryPoints)
                .build();
    }

    @Transactional(readOnly = true)
    public SprintStatisticsResponse getSprintStatistics(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + id));

        List<Task> tasks = taskRepository.findBySprintId(id);

        long todoTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long testingTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TESTING).count();
        long codeReviewTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.CODE_REVIEW).count();
        long doneTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

        long highPriorityTasks = tasks.stream().filter(t -> t.getPriority() == TaskPriority.HIGH).count();
        long criticalPriorityTasks = tasks.stream().filter(t -> t.getPriority() == TaskPriority.CRITICAL).count();

        long totalTasks = tasks.size();
        int totalStoryPoints = tasks.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        double averageStoryPoints = totalTasks == 0 ? 0.0
                : Math.round(((double) totalStoryPoints / totalTasks) * 100.0) / 100.0;

        double completionPercentage = totalTasks == 0 ? 0.0
                : Math.round(((double) doneTasks / totalTasks * 100.0) * 100.0) / 100.0;

        return SprintStatisticsResponse.builder()
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .testingTasks(testingTasks)
                .codeReviewTasks(codeReviewTasks)
                .doneTasks(doneTasks)
                .highPriorityTasks(highPriorityTasks)
                .criticalPriorityTasks(criticalPriorityTasks)
                .averageStoryPoints(averageStoryPoints)
                .completionPercentage(completionPercentage)
                .build();
    }

    private SprintResponse mapToResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .actualStartDate(sprint.getActualStartDate())
                .actualEndDate(sprint.getActualEndDate())
                .status(sprint.getStatus())
                .teamId(sprint.getTeam() != null ? sprint.getTeam().getId() : null)
                .createdAt(sprint.getCreatedAt())
                .createdBy(sprint.getCreatedBy())
                .updatedAt(sprint.getUpdatedAt())
                .updatedBy(sprint.getUpdatedBy())
                .build();
    }

    @Transactional(readOnly = true)
    public SprintProgressResponse getSprintProgress(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + sprintId));

        List<Task> tasks = taskRepository.findBySprintId(sprintId);

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();
        long remainingTasks = totalTasks - completedTasks;

        int totalStoryPoints = tasks.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
        int completedStoryPoints = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
        int remainingStoryPoints = totalStoryPoints - completedStoryPoints;

        double completionPercentage = totalTasks == 0 ? 0.0
                : Math.round(((double) completedTasks / totalTasks * 100.0) * 100.0) / 100.0;

        return SprintProgressResponse.builder()
                .sprintId(sprint.getId())
                .sprintName(sprint.getName())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .remainingTasks(remainingTasks)
                .totalStoryPoints(totalStoryPoints)
                .completedStoryPoints(completedStoryPoints)
                .remainingStoryPoints(remainingStoryPoints)
                .completionPercentage(completionPercentage)
                .currentSprintStatus(sprint.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public List<BurndownPointResponse> getSprintBurndown(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + sprintId));

        List<Task> tasks = taskRepository.findBySprintId(sprintId);

        LocalDate startDate = sprint.getStartDate() != null ? sprint.getStartDate()
                : (sprint.getActualStartDate() != null ? sprint.getActualStartDate()
                : (sprint.getCreatedAt() != null ? sprint.getCreatedAt().toLocalDate() : LocalDate.now()));

        LocalDate endDate = sprint.getEndDate() != null ? sprint.getEndDate()
                : (sprint.getActualEndDate() != null ? sprint.getActualEndDate() : startDate.plusDays(13));

        if (endDate.isBefore(startDate)) {
            endDate = startDate;
        }

        int totalStoryPoints = tasks.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        List<BurndownPointResponse> burndown = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate currentDate = date;
            int completedByDate = tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE)
                    .filter(t -> {
                        LocalDate completionDate = t.getUpdatedAt() != null ? t.getUpdatedAt().toLocalDate()
                                : (t.getCreatedAt() != null ? t.getCreatedAt().toLocalDate() : startDate);
                        return !completionDate.isAfter(currentDate);
                    })
                    .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                    .sum();

            int remainingStoryPoints = Math.max(0, totalStoryPoints - completedByDate);

            burndown.add(BurndownPointResponse.builder()
                    .date(date)
                    .remainingStoryPoints(remainingStoryPoints)
                    .build());
        }

        return burndown;
    }

    @Transactional(readOnly = true)
    public SprintVelocityResponse getSprintVelocity(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + sprintId));

        List<Task> tasks = taskRepository.findBySprintId(sprintId);

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();

        int completedStoryPoints = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        double averageStoryPointsPerTask = completedTasks == 0 ? 0.0
                : Math.round(((double) completedStoryPoints / completedTasks) * 100.0) / 100.0;

        double completionPercentage = totalTasks == 0 ? 0.0
                : Math.round(((double) completedTasks / totalTasks * 100.0) * 100.0) / 100.0;

        return SprintVelocityResponse.builder()
                .completedStoryPoints(completedStoryPoints)
                .completedTasks(completedTasks)
                .averageStoryPointsPerTask(averageStoryPointsPerTask)
                .completionPercentage(completionPercentage)
                .build();
    }

    @Transactional(readOnly = true)
    public TaskDistributionResponse getTaskDistribution(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + sprintId));

        List<Task> tasks = taskRepository.findBySprintId(sprintId);

        Map<TaskStatus, Long> byStatus = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            byStatus.put(status, 0L);
        }
        for (Task task : tasks) {
            if (task.getStatus() != null) {
                byStatus.put(task.getStatus(), byStatus.getOrDefault(task.getStatus(), 0L) + 1);
            }
        }

        Map<TaskPriority, Long> byPriority = new EnumMap<>(TaskPriority.class);
        for (TaskPriority priority : TaskPriority.values()) {
            byPriority.put(priority, 0L);
        }
        for (Task task : tasks) {
            if (task.getPriority() != null) {
                byPriority.put(task.getPriority(), byPriority.getOrDefault(task.getPriority(), 0L) + 1);
            }
        }

        Map<String, Long> byAssignee = new HashMap<>();
        for (Task task : tasks) {
            String assigneeKey = "Unassigned";
            if (task.getAssignee() != null) {
                if (task.getAssignee().getEmail() != null && !task.getAssignee().getEmail().isBlank()) {
                    assigneeKey = task.getAssignee().getEmail();
                } else if (task.getAssignee().getId() != null) {
                    assigneeKey = task.getAssignee().getId().toString();
                }
            }
            byAssignee.put(assigneeKey, byAssignee.getOrDefault(assigneeKey, 0L) + 1);
        }

        return TaskDistributionResponse.builder()
                .byStatus(byStatus)
                .byPriority(byPriority)
                .byAssignee(byAssignee)
                .build();
    }
}
