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
public class DeveloperAnalyticsResponse {

    private Long userId;
    private Long assignedTasks;
    private Long completedTasks;
    private Long todoTasks;
    private Long inProgressTasks;
    private Long codeReviewTasks;
    private Long testingTasks;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private Double completionPercentage;
}
