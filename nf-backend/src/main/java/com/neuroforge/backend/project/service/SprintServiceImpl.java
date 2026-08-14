package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.project.dto.*;
import com.neuroforge.backend.project.entity.Project;
import com.neuroforge.backend.project.entity.Sprint;
import com.neuroforge.backend.project.entity.StoryPointSnapshot;
import com.neuroforge.backend.project.entity.Task;
import com.neuroforge.backend.project.repository.ProjectRepository;
import com.neuroforge.backend.project.repository.SprintRepository;
import com.neuroforge.backend.project.repository.StoryPointSnapshotRepository;
import com.neuroforge.backend.project.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final StoryPointSnapshotRepository snapshotRepository;

    @Override
    @Transactional
    public ApiResponse<SprintDto> createSprint(CreateSprintRequest request) {

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> AppException.notFound("Project not found"));

        Sprint sprint = Sprint.builder()
                .sprintName(request.getSprintName())
                .goal(request.getGoal())
                .status(request.getStatus() == null ? "PLANNED" : request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .project(project)
                .build();

        sprint = sprintRepository.save(sprint);

        return ApiResponse.ok("Sprint created successfully", SprintDto.from(sprint));
    }

    @Override
    public ApiResponse<List<SprintDto>> getAllSprints() {

        List<SprintDto> sprints = sprintRepository.findAll()
                .stream()
                .map(SprintDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok("Sprints retrieved successfully", sprints);
    }

    @Override
    public ApiResponse<List<SprintDto>> getProjectSprints(Long projectId) {

        List<SprintDto> sprints = sprintRepository.findByProjectId(projectId)
                .stream()
                .map(SprintDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok("Project sprints retrieved", sprints);
    }

    @Override
    public ApiResponse<SprintDto> getSprintById(Long id) {

        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        return ApiResponse.ok("Sprint found", SprintDto.from(sprint));
    }

    @Override
    @Transactional
    public ApiResponse<SprintDto> updateSprint(Long id, UpdateSprintRequest request) {

        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        if (request.getSprintName() != null)
            sprint.setSprintName(request.getSprintName());

        if (request.getGoal() != null)
            sprint.setGoal(request.getGoal());

        if (request.getStatus() != null)
            sprint.setStatus(request.getStatus());

        if (request.getStartDate() != null)
            sprint.setStartDate(request.getStartDate());

        if (request.getEndDate() != null)
            sprint.setEndDate(request.getEndDate());

        sprint = sprintRepository.save(sprint);

        return ApiResponse.ok("Sprint updated successfully", SprintDto.from(sprint));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteSprint(Long id) {

        if (!sprintRepository.existsById(id))
            throw AppException.notFound("Sprint not found");

        sprintRepository.deleteById(id);

        return ApiResponse.ok("Sprint deleted successfully");
    }

    // Module 5: Sprint lifecycle methods

    @Override
    @Transactional
    public ApiResponse<SprintDto> startSprint(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        if (!"PLANNED".equals(sprint.getStatus())) {
            throw AppException.badRequest("Only PLANNED sprint may start. Current status: " + sprint.getStatus());
        }

        if (sprintRepository.existsByStatus("ACTIVE")) {
            throw AppException.badRequest("An active sprint already exists.");
        }

        sprint.setStatus("ACTIVE");
        sprint.setActualStartDate(LocalDateTime.now());

        Sprint saved = sprintRepository.save(sprint);
        return ApiResponse.ok("Sprint started successfully", SprintDto.from(saved));
    }

    @Override
    @Transactional
    public ApiResponse<SprintDto> completeSprint(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        if (!"ACTIVE".equals(sprint.getStatus())) {
            throw AppException.badRequest("Only ACTIVE sprint may complete. Current status: " + sprint.getStatus());
        }

        // Set actual end date to current time for timeline synchronization
        sprint.setActualEndDate(LocalDateTime.now());

        // Handle incomplete tasks - move them back to TODO status for next sprint
        List<Task> sprintTasks = taskRepository.findBySprintId(id);
        sprintTasks.stream()
                .filter(t -> !"DONE".equals(t.getStatus()))
                .forEach(t -> {
                    // Reset incomplete tasks to TODO for next sprint
                    t.setStatus("TODO");
                    t.setSprint(null); // Unassign from completed sprint
                    taskRepository.save(t);
                });

        sprint.setStatus("COMPLETED");
        Sprint saved = sprintRepository.save(sprint);
        return ApiResponse.ok("Sprint completed successfully", SprintDto.from(saved));
    }

    // Module 5: Sprint analytics methods

    @Override
    public ApiResponse<SprintSummaryResponse> getSprintSummary(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        List<Task> tasks = taskRepository.findBySprintId(id);

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()))
                .count();
        long remainingTasks = totalTasks - completedTasks;

        double completionPercentage = totalTasks == 0 ? 0.0
                : Math.round(((double) completedTasks / totalTasks * 100.0) * 100.0) / 100.0;

        int totalStoryPoints = tasks.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
        int completedStoryPoints = tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()))
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
        int remainingStoryPoints = totalStoryPoints - completedStoryPoints;

        // Module 4: Requirement traceability statistics
        long tasksWithRequirements = tasks.stream()
                .filter(t -> t.getSpecificationId() != null)
                .count();
        long completedTasksWithRequirements = tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()))
                .filter(t -> t.getSpecificationId() != null)
                .count();

        SprintSummaryResponse response = SprintSummaryResponse.builder()
                .id(sprint.getId())
                .sprintName(sprint.getSprintName())
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
                .tasksWithRequirements(tasksWithRequirements)
                .completedTasksWithRequirements(completedTasksWithRequirements)
                .build();

        return ApiResponse.ok("Sprint summary retrieved", response);
    }

    @Override
    public ApiResponse<SprintStatisticsResponse> getSprintStatistics(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        List<Task> tasks = taskRepository.findBySprintId(id);

        long todoTasks = tasks.stream().filter(t -> "TODO".equals(t.getStatus())).count();
        long inProgressTasks = tasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long testingTasks = tasks.stream().filter(t -> "TESTING".equals(t.getStatus())).count();
        long codeReviewTasks = tasks.stream().filter(t -> "CODE_REVIEW".equals(t.getStatus())).count();
        long doneTasks = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();

        long highPriorityTasks = tasks.stream().filter(t -> "HIGH".equals(t.getPriority())).count();
        long criticalPriorityTasks = tasks.stream().filter(t -> "CRITICAL".equals(t.getPriority())).count();

        long totalTasks = tasks.size();
        int totalStoryPoints = tasks.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        double averageStoryPoints = totalTasks == 0 ? 0.0
                : Math.round(((double) totalStoryPoints / totalTasks) * 100.0) / 100.0;

        double completionPercentage = totalTasks == 0 ? 0.0
                : Math.round(((double) doneTasks / totalTasks * 100.0) * 100.0) / 100.0;

        SprintStatisticsResponse response = SprintStatisticsResponse.builder()
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

        return ApiResponse.ok("Sprint statistics retrieved", response);
    }

    @Override
    public ApiResponse<SprintProgressResponse> getSprintProgress(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        List<Task> tasks = taskRepository.findBySprintId(sprintId);

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()))
                .count();
        long remainingTasks = totalTasks - completedTasks;

        int totalStoryPoints = tasks.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
        int completedStoryPoints = tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()))
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
        int remainingStoryPoints = totalStoryPoints - completedStoryPoints;

        double completionPercentage = totalTasks == 0 ? 0.0
                : Math.round(((double) completedTasks / totalTasks * 100.0) * 100.0) / 100.0;

        SprintProgressResponse response = SprintProgressResponse.builder()
                .sprintId(sprint.getId())
                .sprintName(sprint.getSprintName())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .remainingTasks(remainingTasks)
                .totalStoryPoints(totalStoryPoints)
                .completedStoryPoints(completedStoryPoints)
                .remainingStoryPoints(remainingStoryPoints)
                .completionPercentage(completionPercentage)
                .currentSprintStatus(sprint.getStatus())
                .build();

        return ApiResponse.ok("Sprint progress retrieved", response);
    }

    @Override
    public ApiResponse<List<BurndownPointResponse>> getSprintBurndown(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        List<Task> tasks = taskRepository.findBySprintId(sprintId);

        LocalDateTime startDate = sprint.getStartDate() != null ? sprint.getStartDate()
                : (sprint.getActualStartDate() != null ? sprint.getActualStartDate()
                : sprint.getCreatedAt());

        LocalDateTime endDate = sprint.getEndDate() != null ? sprint.getEndDate()
                : (sprint.getActualEndDate() != null ? sprint.getActualEndDate()
                : (startDate != null ? startDate.plusDays(14) : LocalDateTime.now()));

        // If no dates available, use current date as start and 14 days as end
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        if (endDate == null || endDate.isBefore(startDate)) {
            endDate = startDate.plusDays(14);
        }

        // Create effectively final copy for lambda use
        final LocalDateTime finalStartDate = startDate;

        int totalStoryPoints = tasks.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        // Module 5: Use story point snapshots for accurate burndown calculation
        List<StoryPointSnapshot> snapshots = snapshotRepository.findBySprintIdOrderBySnapshotDateAsc(sprintId);
        
        List<BurndownPointResponse> burndown = new ArrayList<>();
        for (LocalDateTime date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate currentDate = date.toLocalDate();
            int remainingStoryPoints;
            
            // Try to use snapshot data if available
            StoryPointSnapshot snapshot = snapshots.stream()
                    .filter(s -> s.getSnapshotDate().equals(currentDate))
                    .findFirst()
                    .orElse(null);
            
            if (snapshot != null) {
                // Use snapshot data
                remainingStoryPoints = snapshot.getRemainingStoryPoints();
            } else {
                // Fallback to calculation from task timestamps
                LocalDateTime currentDateTime = date;
                int completedByDate = tasks.stream()
                        .filter(t -> "DONE".equals(t.getStatus()))
                        .filter(t -> {
                            LocalDateTime completionDate = t.getUpdatedAt() != null ? t.getUpdatedAt()
                                    : (t.getCreatedAt() != null ? t.getCreatedAt() : finalStartDate);
                            return !completionDate.isAfter(currentDateTime);
                        })
                        .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                        .sum();
                
                remainingStoryPoints = Math.max(0, totalStoryPoints - completedByDate);
            }

            burndown.add(BurndownPointResponse.builder()
                    .date(currentDate)
                    .remainingStoryPoints(remainingStoryPoints)
                    .build());
        }

        return ApiResponse.ok("Sprint burndown retrieved", burndown);
    }

    @Override
    public ApiResponse<SprintVelocityResponse> getSprintVelocity(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        List<Task> tasks = taskRepository.findBySprintId(sprintId);

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()))
                .count();

        int completedStoryPoints = tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()))
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        double averageStoryPointsPerTask = completedTasks == 0 ? 0.0
                : Math.round(((double) completedStoryPoints / completedTasks) * 100.0) / 100.0;

        double completionPercentage = totalTasks == 0 ? 0.0
                : Math.round(((double) completedTasks / totalTasks * 100.0) * 100.0) / 100.0;

        SprintVelocityResponse response = SprintVelocityResponse.builder()
                .completedStoryPoints(completedStoryPoints)
                .completedTasks(completedTasks)
                .averageStoryPointsPerTask(averageStoryPointsPerTask)
                .completionPercentage(completionPercentage)
                .build();

        return ApiResponse.ok("Sprint velocity retrieved", response);
    }

    @Override
    public ApiResponse<TaskDistributionResponse> getTaskDistribution(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        List<Task> tasks = taskRepository.findBySprintId(sprintId);

        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("TODO", 0L);
        byStatus.put("IN_PROGRESS", 0L);
        byStatus.put("CODE_REVIEW", 0L);
        byStatus.put("TESTING", 0L);
        byStatus.put("DONE", 0L);
        for (Task task : tasks) {
            if (task.getStatus() != null) {
                byStatus.put(task.getStatus(), byStatus.getOrDefault(task.getStatus(), 0L) + 1);
            }
        }

        Map<String, Long> byPriority = new HashMap<>();
        byPriority.put("LOW", 0L);
        byPriority.put("MEDIUM", 0L);
        byPriority.put("HIGH", 0L);
        byPriority.put("CRITICAL", 0L);
        for (Task task : tasks) {
            if (task.getPriority() != null) {
                byPriority.put(task.getPriority(), byPriority.getOrDefault(task.getPriority(), 0L) + 1);
            }
        }

        Map<String, Long> byAssignee = new HashMap<>();
        for (Task task : tasks) {
            String assigneeKey = "Unassigned";
            if (task.getAssignedTo() != null) {
                assigneeKey = "Member-" + task.getAssignedTo().getId();
            }
            byAssignee.put(assigneeKey, byAssignee.getOrDefault(assigneeKey, 0L) + 1);
        }

        TaskDistributionResponse response = TaskDistributionResponse.builder()
                .byStatus(byStatus)
                .byPriority(byPriority)
                .byAssignee(byAssignee)
                .build();

        return ApiResponse.ok("Task distribution retrieved", response);
    }
}