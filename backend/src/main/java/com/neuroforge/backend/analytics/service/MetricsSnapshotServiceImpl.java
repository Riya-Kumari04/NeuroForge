package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.MetricsSnapshotResponse;
import com.neuroforge.backend.analytics.entity.DeploymentRecord;
import com.neuroforge.backend.analytics.entity.MetricsSnapshot;
import com.neuroforge.backend.analytics.enums.DeploymentEnvironment;
import com.neuroforge.backend.analytics.enums.DeploymentStatus;
import com.neuroforge.backend.analytics.repository.DeploymentRecordRepository;
import com.neuroforge.backend.analytics.repository.MetricsSnapshotRepository;
import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.entity.TaskStatusHistory;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.mongodb.document.ReviewDocument;
import com.neuroforge.backend.mongodb.document.ReviewIssue;
import com.neuroforge.backend.mongodb.repository.ReviewDocumentRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.TaskStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricsSnapshotServiceImpl implements MetricsSnapshotService {

    private final MetricsSnapshotRepository metricsSnapshotRepository;
    private final TaskRepository taskRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final ReviewDocumentRepository reviewDocumentRepository;
    private final DeploymentRecordRepository deploymentRecordRepository;

    @Override
    public MetricsSnapshotResponse createOrUpdateSnapshot(LocalDate snapshotDate) {
        if (snapshotDate == null) {
            snapshotDate = LocalDate.now();
        }

        // 1. Task Metrics
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByStatus(TaskStatus.DONE);
        long remainingTasks = totalTasks - completedTasks;
        Integer totalStoryPointsObj = taskRepository.getTotalStoryPoints();
        int totalStoryPoints = totalStoryPointsObj != null ? totalStoryPointsObj : 0;
        Integer completedStoryPointsObj = taskRepository.getStoryPointsByStatus(TaskStatus.DONE);
        int completedStoryPoints = completedStoryPointsObj != null ? completedStoryPointsObj : 0;
        double completionPercentage = totalTasks > 0 ? Math.round(((completedTasks * 100.0) / totalTasks) * 100.0) / 100.0 : 0.0;

        // 2. Issue Metrics
        List<ReviewDocument> reviewDocs = reviewDocumentRepository.findAllByOrderByCreatedAtAsc();
        long highIssues = 0;
        long mediumIssues = 0;
        long lowIssues = 0;
        long infoIssues = 0;

        if (reviewDocs != null) {
            for (ReviewDocument doc : reviewDocs) {
                if (doc != null && doc.getIssues() != null) {
                    for (ReviewIssue issue : doc.getIssues()) {
                        if (issue != null && issue.getSeverity() != null) {
                            switch (issue.getSeverity()) {
                                case HIGH -> highIssues++;
                                case MEDIUM -> mediumIssues++;
                                case LOW -> lowIssues++;
                                case INFO -> infoIssues++;
                            }
                        }
                    }
                }
            }
        }
        long totalIssues = highIssues + mediumIssues + lowIssues + infoIssues;

        // 3. Cycle Time Metric
        List<Task> doneTasks = taskRepository.findByStatus(TaskStatus.DONE);
        long measuredCount = 0;
        double totalMinutes = 0.0;

        if (doneTasks != null) {
            for (Task task : doneTasks) {
                if (task == null || task.getId() == null) {
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
                    long minutes = Duration.between(startedAt, completedAt).toMinutes();
                    totalMinutes += minutes;
                    measuredCount++;
                }
            }
        }

        double averageCycleTimeHours = 0.0;
        if (measuredCount > 0) {
            double avgHours = (totalMinutes / (double) measuredCount) / 60.0;
            averageCycleTimeHours = Math.round(avgHours * 100.0) / 100.0;
        }

        // 4. Deployment Metrics (30-day window)
        LocalDate periodStart = snapshotDate.minusDays(29);
        LocalDate periodEnd = snapshotDate;
        LocalDateTime startTimestamp = periodStart.atStartOfDay();
        LocalDateTime endTimestamp = periodEnd.plusDays(1).atStartOfDay();

        long successfulDeployments = deploymentRecordRepository.countByEnvironmentAndStatusAndDeployedAtBetween(
                DeploymentEnvironment.PRODUCTION,
                DeploymentStatus.SUCCESS,
                startTimestamp,
                endTimestamp
        );

        List<DeploymentRecord> prodDeployments = deploymentRecordRepository.findByEnvironmentAndDeployedAtBetween(
                DeploymentEnvironment.PRODUCTION,
                startTimestamp,
                endTimestamp
        );

        long productionDeploymentAttempts = prodDeployments != null ? prodDeployments.size() : 0;
        long failedProductionDeployments = prodDeployments != null ? prodDeployments.stream().filter(d -> d.getStatus() == DeploymentStatus.FAILED).count() : 0;

        double deploymentFrequencyPerDay = 0.0;
        if (successfulDeployments > 0) {
            deploymentFrequencyPerDay = Math.round((successfulDeployments / 30.0) * 100.0) / 100.0;
        }

        double changeFailureRate = 0.0;
        if (productionDeploymentAttempts > 0) {
            double rawRate = (failedProductionDeployments * 100.0) / productionDeploymentAttempts;
            changeFailureRate = Math.round(rawRate * 100.0) / 100.0;
        }

        // 5. Persistence
        final LocalDate targetDate = snapshotDate;
        MetricsSnapshot snapshot = metricsSnapshotRepository.findBySnapshotDate(targetDate)
                .orElseGet(() -> MetricsSnapshot.builder().snapshotDate(targetDate).build());

        snapshot.setTotalTasks(totalTasks);
        snapshot.setCompletedTasks(completedTasks);
        snapshot.setRemainingTasks(remainingTasks);
        snapshot.setTotalStoryPoints(totalStoryPoints);
        snapshot.setCompletedStoryPoints(completedStoryPoints);
        snapshot.setCompletionPercentage(completionPercentage);
        snapshot.setAverageCycleTimeHours(averageCycleTimeHours);
        snapshot.setTotalIssues(totalIssues);
        snapshot.setHighIssues(highIssues);
        snapshot.setMediumIssues(mediumIssues);
        snapshot.setLowIssues(lowIssues);
        snapshot.setInfoIssues(infoIssues);
        snapshot.setSuccessfulDeployments(successfulDeployments);
        snapshot.setProductionDeploymentAttempts(productionDeploymentAttempts);
        snapshot.setFailedProductionDeployments(failedProductionDeployments);
        snapshot.setDeploymentFrequencyPerDay(deploymentFrequencyPerDay);
        snapshot.setChangeFailureRate(changeFailureRate);

        MetricsSnapshot saved = metricsSnapshotRepository.save(snapshot);
        return mapToResponse(saved);
    }

    @Override
    public MetricsSnapshotResponse getSnapshot(LocalDate snapshotDate) {
        MetricsSnapshot snapshot = metricsSnapshotRepository.findBySnapshotDate(snapshotDate)
                .orElseThrow(() -> new ResourceNotFoundException("Metrics snapshot not found for date: " + snapshotDate));
        return mapToResponse(snapshot);
    }

    @Override
    public List<MetricsSnapshotResponse> getSnapshots(LocalDate startDate, LocalDate endDate) {
        List<MetricsSnapshot> snapshots = metricsSnapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(startDate, endDate);
        return snapshots.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private MetricsSnapshotResponse mapToResponse(MetricsSnapshot snapshot) {
        return MetricsSnapshotResponse.builder()
                .id(snapshot.getId())
                .snapshotDate(snapshot.getSnapshotDate())
                .totalTasks(snapshot.getTotalTasks())
                .completedTasks(snapshot.getCompletedTasks())
                .remainingTasks(snapshot.getRemainingTasks())
                .totalStoryPoints(snapshot.getTotalStoryPoints())
                .completedStoryPoints(snapshot.getCompletedStoryPoints())
                .completionPercentage(snapshot.getCompletionPercentage())
                .averageCycleTimeHours(snapshot.getAverageCycleTimeHours())
                .totalIssues(snapshot.getTotalIssues())
                .highIssues(snapshot.getHighIssues())
                .mediumIssues(snapshot.getMediumIssues())
                .lowIssues(snapshot.getLowIssues())
                .infoIssues(snapshot.getInfoIssues())
                .successfulDeployments(snapshot.getSuccessfulDeployments())
                .productionDeploymentAttempts(snapshot.getProductionDeploymentAttempts())
                .failedProductionDeployments(snapshot.getFailedProductionDeployments())
                .deploymentFrequencyPerDay(snapshot.getDeploymentFrequencyPerDay())
                .changeFailureRate(snapshot.getChangeFailureRate())
                .build();
    }
}
