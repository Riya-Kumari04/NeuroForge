package com.neuroforge.backend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsSnapshotResponse {

    private UUID id;
    private LocalDate snapshotDate;
    private Long totalTasks;
    private Long completedTasks;
    private Long remainingTasks;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private Double completionPercentage;
    private Double averageCycleTimeHours;
    private Long totalIssues;
    private Long highIssues;
    private Long mediumIssues;
    private Long lowIssues;
    private Long infoIssues;
    private Long successfulDeployments;
    private Long productionDeploymentAttempts;
    private Long failedProductionDeployments;
    private Double deploymentFrequencyPerDay;
    private Double changeFailureRate;
}
