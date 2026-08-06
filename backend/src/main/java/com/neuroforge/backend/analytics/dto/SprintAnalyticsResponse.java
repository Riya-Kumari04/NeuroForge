package com.neuroforge.backend.analytics.dto;

import com.neuroforge.backend.enums.SprintStatus;
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
public class SprintAnalyticsResponse {

    private UUID sprintId;
    private String sprintName;
    private SprintStatus sprintStatus;
    private Long totalTasks;
    private Long completedTasks;
    private Long remainingTasks;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private Double completionPercentage;
}
