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
public class SprintAnalyticsResponse {

    private Long sprintId;
    private String sprintName;
    private String sprintStatus;
    private Long totalTasks;
    private Long completedTasks;
    private Long remainingTasks;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private Double completionPercentage;
}
