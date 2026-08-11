package com.neuroforge.backend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectHealthSummary {

    private UUID projectId;
    private String projectName;
    private UUID teamId;
    private String healthStatus;
    private Long totalTasks;
    private Long completedTasks;
    private Double completionPercentage;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private Integer activeSprints;
    private Integer completedSprints;
}
