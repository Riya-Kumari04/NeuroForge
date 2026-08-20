package com.neuroforge.backend.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "metrics_snapshots", indexes = {
    @Index(name = "idx_snapshot_date", columnList = "snapshot_date", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "snapshot_date", nullable = false, unique = true)
    private LocalDate snapshotDate;

    @Column(name = "total_tasks")
    private Long totalTasks;

    @Column(name = "completed_tasks")
    private Long completedTasks;

    @Column(name = "remaining_tasks")
    private Long remainingTasks;

    @Column(name = "total_story_points")
    private Integer totalStoryPoints;

    @Column(name = "completed_story_points")
    private Integer completedStoryPoints;

    @Column(name = "completion_percentage")
    private Double completionPercentage;

    @Column(name = "average_cycle_time_hours")
    private Double averageCycleTimeHours;

    @Column(name = "total_issues")
    private Long totalIssues;

    @Column(name = "high_issues")
    private Long highIssues;

    @Column(name = "medium_issues")
    private Long mediumIssues;

    @Column(name = "low_issues")
    private Long lowIssues;

    @Column(name = "info_issues")
    private Long infoIssues;

    @Column(name = "successful_deployments")
    private Long successfulDeployments;

    @Column(name = "production_deployment_attempts")
    private Long productionDeploymentAttempts;

    @Column(name = "failed_production_deployments")
    private Long failedProductionDeployments;

    @Column(name = "deployment_frequency_per_day")
    private Double deploymentFrequencyPerDay;

    @Column(name = "change_failure_rate")
    private Double changeFailureRate;
}
