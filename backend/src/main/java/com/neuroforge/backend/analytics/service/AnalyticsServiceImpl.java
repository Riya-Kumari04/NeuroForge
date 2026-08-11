package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.AnalyticsDashboardResponse;
import com.neuroforge.backend.analytics.dto.BurndownResponse;
import com.neuroforge.backend.analytics.dto.DeveloperAnalyticsResponse;
import com.neuroforge.backend.analytics.dto.SprintAnalyticsResponse;
import com.neuroforge.backend.analytics.dto.TaskDistributionResponse;
import com.neuroforge.backend.analytics.dto.VelocityPointResponse;
import com.neuroforge.backend.analytics.dto.VelocityResponse;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.CodeReviewRepository;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.TaskStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final CodeReviewRepository codeReviewRepository;

    @Override
    public AnalyticsDashboardResponse getDashboard() {
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByStatus(TaskStatus.DONE);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long codeReviewTasks = taskRepository.countByStatus(TaskStatus.CODE_REVIEW);
        long testingTasks = taskRepository.countByStatus(TaskStatus.TESTING);
        long todoTasks = taskRepository.countByStatus(TaskStatus.TODO);

        Integer totalStoryPoints = taskRepository.getTotalStoryPoints();
        Integer completedStoryPoints = taskRepository.getStoryPointsByStatus(TaskStatus.DONE);

        double completionPercentage = 0.0;
        if (totalTasks > 0) {
            double rawPercentage = (completedTasks * 100.0) / totalTasks;
            completionPercentage = Math.round(rawPercentage * 100.0) / 100.0;
        }

        return AnalyticsDashboardResponse.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .codeReviewTasks(codeReviewTasks)
                .testingTasks(testingTasks)
                .todoTasks(todoTasks)
                .totalStoryPoints(totalStoryPoints)
                .completedStoryPoints(completedStoryPoints)
                .completionPercentage(completionPercentage)
                .build();
    }

    @Override
    public SprintAnalyticsResponse getSprintAnalytics(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found."));

        long totalTasks = taskRepository.countBySprintId(sprintId);
        long completedTasks = taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE);
        long remainingTasks = totalTasks - completedTasks;

        Integer totalStoryPoints = taskRepository.getTotalStoryPointsBySprint(sprintId);
        Integer completedStoryPoints = taskRepository.getStoryPointsBySprintAndStatus(sprintId, TaskStatus.DONE);

        double completionPercentage = 0.0;
        if (totalTasks > 0) {
            double rawPercentage = (completedTasks * 100.0) / totalTasks;
            completionPercentage = Math.round(rawPercentage * 100.0) / 100.0;
        }

        return SprintAnalyticsResponse.builder()
                .sprintId(sprint.getId())
                .sprintName(sprint.getName())
                .sprintStatus(sprint.getStatus())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .remainingTasks(remainingTasks)
                .totalStoryPoints(totalStoryPoints)
                .completedStoryPoints(completedStoryPoints)
                .completionPercentage(completionPercentage)
                .build();
    }

    @Override
    public DeveloperAnalyticsResponse getDeveloperAnalytics(Long userId) {
        long assignedTasks = taskRepository.countByAssigneeId(userId);
        long completedTasks = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.DONE);
        long todoTasks = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        long codeReviewTasks = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.CODE_REVIEW);
        long testingTasks = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.TESTING);

        Integer totalStoryPoints = taskRepository.getTotalStoryPointsByAssignee(userId);
        Integer completedStoryPoints = taskRepository.getStoryPointsByAssigneeAndStatus(userId, TaskStatus.DONE);

        double completionPercentage = 0.0;
        if (assignedTasks > 0) {
            double rawPercentage = (completedTasks * 100.0) / assignedTasks;
            completionPercentage = Math.round(rawPercentage * 100.0) / 100.0;
        }

        return DeveloperAnalyticsResponse.builder()
                .userId(userId)
                .assignedTasks(assignedTasks)
                .completedTasks(completedTasks)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .codeReviewTasks(codeReviewTasks)
                .testingTasks(testingTasks)
                .totalStoryPoints(totalStoryPoints)
                .completedStoryPoints(completedStoryPoints)
                .completionPercentage(completionPercentage)
                .build();
    }

    @Override
    public TaskDistributionResponse getTaskDistribution() {
        long todoTasks = taskRepository.countByStatus(TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long codeReviewTasks = taskRepository.countByStatus(TaskStatus.CODE_REVIEW);
        long testingTasks = taskRepository.countByStatus(TaskStatus.TESTING);
        long completedTasks = taskRepository.countByStatus(TaskStatus.DONE);

        return TaskDistributionResponse.builder()
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .codeReviewTasks(codeReviewTasks)
                .testingTasks(testingTasks)
                .completedTasks(completedTasks)
                .build();
    }

    @Override
    public VelocityResponse getVelocity() {
        List<Sprint> sprints = sprintRepository.findAllByOrderByStartDateAsc();

        List<VelocityPointResponse> points = sprints.stream()
                .filter(sprint -> sprint.getStatus() == SprintStatus.COMPLETED)
                .map(sprint -> {
                    UUID sprintId = sprint.getId();
                    String sprintName = sprint.getName();
                    Integer completedStoryPoints = taskRepository.getStoryPointsBySprintAndStatus(sprintId, TaskStatus.DONE);
                    long completedTasks = taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE);
                    LocalDate sprintEndDate = sprint.getActualEndDate() != null ? sprint.getActualEndDate() : sprint.getEndDate();

                    return VelocityPointResponse.builder()
                            .sprintId(sprintId)
                            .sprintName(sprintName)
                            .completedStoryPoints(completedStoryPoints != null ? completedStoryPoints : 0)
                            .completedTasks(completedTasks)
                            .sprintEndDate(sprintEndDate)
                            .build();
                })
                .collect(Collectors.toList());

        return VelocityResponse.builder()
                .sprints(points)
                .build();
    }

    @Override
    public BurndownResponse getBurndown() {
        // TODO Implement analytics calculation
        return BurndownResponse.builder().build();
    }
}
