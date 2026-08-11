package com.neuroforge.backend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioHealthResponse {

    private UUID organizationId;
    private String organizationName;
    private Integer totalProjects;
    private Integer healthyProjects;
    private Integer atRiskProjects;
    private Integer criticalProjects;
    private Double overallCompletionPercentage;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private Long totalTasks;
    private Long completedTasks;
    private List<ProjectHealthSummary> projects;
}
