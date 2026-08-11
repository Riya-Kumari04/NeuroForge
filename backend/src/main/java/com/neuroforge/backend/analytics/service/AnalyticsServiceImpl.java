package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.AnalyticsDashboardResponse;
import com.neuroforge.backend.analytics.dto.BurndownPointResponse;
import com.neuroforge.backend.analytics.dto.BurndownResponse;
import com.neuroforge.backend.analytics.dto.ChangeFailureRateResponse;
import com.neuroforge.backend.analytics.dto.CycleTimePointResponse;
import com.neuroforge.backend.analytics.dto.CycleTimeResponse;
import com.neuroforge.backend.analytics.dto.DeveloperAnalyticsResponse;
import com.neuroforge.backend.analytics.dto.DeploymentFrequencyResponse;
import com.neuroforge.backend.analytics.dto.IssueTrendPointResponse;
import com.neuroforge.backend.analytics.dto.IssueTrendResponse;
import com.neuroforge.backend.analytics.dto.SprintAnalyticsResponse;
import com.neuroforge.backend.analytics.dto.TaskDistributionResponse;
import com.neuroforge.backend.analytics.dto.VelocityPointResponse;
import com.neuroforge.backend.analytics.dto.VelocityResponse;
import com.neuroforge.backend.analytics.entity.DeploymentRecord;
import com.neuroforge.backend.analytics.enums.DeploymentEnvironment;
import com.neuroforge.backend.analytics.enums.DeploymentStatus;
import com.neuroforge.backend.analytics.repository.DeploymentRecordRepository;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.entity.TaskStatusHistory;
import com.neuroforge.backend.enums.IssueSeverity;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.mongodb.document.ReviewDocument;
import com.neuroforge.backend.mongodb.document.ReviewIssue;
import com.neuroforge.backend.mongodb.repository.ReviewDocumentRepository;
import com.neuroforge.backend.repository.CodeReviewRepository;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.TaskStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final CodeReviewRepository codeReviewRepository;
    private final ReviewDocumentRepository reviewDocumentRepository;
    private final DeploymentRecordRepository deploymentRecordRepository;

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
        Sprint sprint = sprintRepository.findFirstByStatus(SprintStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active sprint found."));

        List<Task> tasks = taskRepository.findBySprintId(sprint.getId());

        LocalDate startDate = sprint.getStartDate();
        if (startDate == null) {
            startDate = sprint.getActualStartDate();
        }
        if (startDate == null && sprint.getCreatedAt() != null) {
            startDate = sprint.getCreatedAt().toLocalDate();
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }

        LocalDate endDate = sprint.getEndDate();
        if (endDate == null) {
            endDate = sprint.getActualEndDate();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(13);
        }

        if (endDate.isBefore(startDate)) {
            endDate = startDate;
        }

        int totalStoryPoints = tasks.stream()
                .mapToInt(task -> task.getStoryPoints() != null ? task.getStoryPoints() : 0)
                .sum();

        List<BurndownPointResponse> points = new ArrayList<>();
        final LocalDate sprintStartDate = startDate;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            int completedStoryPoints = tasks.stream()
                    .filter(task -> task.getStatus() == TaskStatus.DONE)
                    .filter(task -> {
                        LocalDate completionDate = task.getUpdatedAt() != null
                                ? task.getUpdatedAt().toLocalDate()
                                : (task.getCreatedAt() != null ? task.getCreatedAt().toLocalDate() : sprintStartDate);
                        return !completionDate.isAfter(currentDate);
                    })
                    .mapToInt(task -> task.getStoryPoints() != null ? task.getStoryPoints() : 0)
                    .sum();

            int remainingStoryPoints = Math.max(0, totalStoryPoints - completedStoryPoints);

            points.add(BurndownPointResponse.builder()
                    .date(currentDate)
                    .remainingStoryPoints(remainingStoryPoints)
                    .completedStoryPoints(completedStoryPoints)
                    .build());
        }

        return BurndownResponse.builder()
                .sprintId(sprint.getId())
                .sprintName(sprint.getName())
                .startDate(startDate)
                .endDate(endDate)
                .totalStoryPoints(totalStoryPoints)
                .points(points)
                .build();
    }

    @Override
    public IssueTrendResponse getIssueTrend() {
        List<ReviewDocument> documents = reviewDocumentRepository.findAllByOrderByCreatedAtAsc();

        if (documents.isEmpty()) {
            return IssueTrendResponse.builder()
                    .points(Collections.emptyList())
                    .build();
        }

        Map<LocalDate, Map<IssueSeverity, Integer>> countsByDate = new TreeMap<>();

        for (ReviewDocument doc : documents) {
            if (doc.getCreatedAt() == null || doc.getIssues() == null || doc.getIssues().isEmpty()) {
                continue;
            }

            LocalDate date = doc.getCreatedAt().toLocalDate();

            for (ReviewIssue issue : doc.getIssues()) {
                if (issue == null || issue.getSeverity() == null) {
                    continue;
                }

                countsByDate.computeIfAbsent(date, d -> new EnumMap<>(IssueSeverity.class))
                        .merge(issue.getSeverity(), 1, Integer::sum);
            }
        }

        List<IssueTrendPointResponse> points = countsByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    Map<IssueSeverity, Integer> severityMap = entry.getValue();

                    int high = severityMap.getOrDefault(IssueSeverity.HIGH, 0);
                    int medium = severityMap.getOrDefault(IssueSeverity.MEDIUM, 0);
                    int low = severityMap.getOrDefault(IssueSeverity.LOW, 0);
                    int info = severityMap.getOrDefault(IssueSeverity.INFO, 0);
                    int total = high + medium + low + info;

                    return IssueTrendPointResponse.builder()
                            .date(date)
                            .totalIssues(total)
                            .highIssues(high)
                            .mediumIssues(medium)
                            .lowIssues(low)
                            .infoIssues(info)
                            .build();
                })
                .collect(Collectors.toList());

        return IssueTrendResponse.builder()
                .points(points)
                .build();
    }

    @Override
    public CycleTimeResponse getCycleTime() {
        List<Task> doneTasks = taskRepository.findByStatus(TaskStatus.DONE);
        long completedTasks = doneTasks.size();

        List<CycleTimePointResponse> points = new ArrayList<>();

        for (Task task : doneTasks) {
            if (task.getId() == null) {
                continue;
            }

            List<TaskStatusHistory> historyList = taskStatusHistoryRepository.findByTaskIdOrderByChangedAtAsc(task.getId());
            if (historyList == null || historyList.isEmpty()) {
                continue;
            }

            LocalDateTime startedAt = null;
            LocalDateTime completedAt = null;

            for (TaskStatusHistory history : historyList) {
                if (history == null || history.getChangedAt() == null || history.getNewStatus() == null) {
                    continue;
                }

                if (startedAt == null && history.getNewStatus() == TaskStatus.IN_PROGRESS) {
                    startedAt = history.getChangedAt();
                } else if (startedAt != null && history.getNewStatus() == TaskStatus.DONE) {
                    if (!history.getChangedAt().isBefore(startedAt)) {
                        completedAt = history.getChangedAt();
                        break;
                    }
                }
            }

            if (startedAt != null && completedAt != null && !completedAt.isBefore(startedAt)) {
                long cycleTimeMinutes = Duration.between(startedAt, completedAt).toMinutes();

                points.add(CycleTimePointResponse.builder()
                        .taskId(task.getId())
                        .taskTitle(task.getTitle())
                        .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                        .startedAt(startedAt)
                        .completedAt(completedAt)
                        .cycleTimeMinutes(cycleTimeMinutes)
                        .build());
            }
        }

        long measuredTasks = points.size();
        double averageCycleTimeHours = 0.0;

        if (measuredTasks > 0) {
            double totalMinutes = points.stream()
                    .mapToLong(CycleTimePointResponse::getCycleTimeMinutes)
                    .sum();
            double avgHours = (totalMinutes / (double) measuredTasks) / 60.0;
            averageCycleTimeHours = Math.round(avgHours * 100.0) / 100.0;

            points.sort(Comparator.comparing(CycleTimePointResponse::getCompletedAt));
        } else {
            points = Collections.emptyList();
        }

        return CycleTimeResponse.builder()
                .averageCycleTimeHours(averageCycleTimeHours)
                .completedTasks(completedTasks)
                .measuredTasks(measuredTasks)
                .points(points)
                .build();
    }

    @Override
    public DeploymentFrequencyResponse getDeploymentFrequency() {
        LocalDate periodEnd = LocalDate.now();
        LocalDate periodStart = periodEnd.minusDays(29);
        LocalDateTime startTimestamp = periodStart.atStartOfDay();
        LocalDateTime endTimestamp = periodEnd.plusDays(1).atStartOfDay();

        long totalSuccessful = deploymentRecordRepository.countByEnvironmentAndStatusAndDeployedAtBetween(
                DeploymentEnvironment.PRODUCTION,
                DeploymentStatus.SUCCESS,
                startTimestamp,
                endTimestamp
        );

        double deploymentsPerDay = 0.0;
        double deploymentsPerWeek = 0.0;

        if (totalSuccessful > 0) {
            double rawPerDay = totalSuccessful / 30.0;
            deploymentsPerDay = Math.round(rawPerDay * 100.0) / 100.0;
            double rawPerWeek = deploymentsPerDay * 7.0;
            deploymentsPerWeek = Math.round(rawPerWeek * 100.0) / 100.0;
        }

        return DeploymentFrequencyResponse.builder()
                .totalSuccessfulDeployments(totalSuccessful)
                .periodDays(30)
                .deploymentsPerDay(deploymentsPerDay)
                .deploymentsPerWeek(deploymentsPerWeek)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .build();
    }

    @Override
    public ChangeFailureRateResponse getChangeFailureRate() {
        LocalDate periodEnd = LocalDate.now();
        LocalDate periodStart = periodEnd.minusDays(29);
        LocalDateTime startTimestamp = periodStart.atStartOfDay();
        LocalDateTime endTimestamp = periodEnd.plusDays(1).atStartOfDay();

        List<DeploymentRecord> prodDeployments = deploymentRecordRepository.findByEnvironmentAndDeployedAtBetween(
                DeploymentEnvironment.PRODUCTION,
                startTimestamp,
                endTimestamp
        );

        long totalAttempts = prodDeployments.size();
        long failedDeployments = prodDeployments.stream()
                .filter(d -> d.getStatus() == DeploymentStatus.FAILED)
                .count();

        double failureRate = 0.0;
        if (totalAttempts > 0) {
            double rawRate = (failedDeployments * 100.0) / totalAttempts;
            failureRate = Math.round(rawRate * 100.0) / 100.0;
        }

        return ChangeFailureRateResponse.builder()
                .totalProductionDeploymentAttempts(totalAttempts)
                .failedProductionDeployments(failedDeployments)
                .changeFailureRate(failureRate)
                .periodDays(30)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .build();
    }
}
