package com.neuroforge.backend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectHealthSummary {

    private Long projectId;
    private String projectName;
    private Long teamId;
    private String healthStatus;
    private Long totalTasks;
    private Long completedTasks;
    private Double completionPercentage;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private Integer activeSprints;
    private Integer completedSprints;
}
