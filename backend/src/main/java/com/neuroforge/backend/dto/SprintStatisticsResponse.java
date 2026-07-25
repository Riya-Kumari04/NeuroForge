package com.neuroforge.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintStatisticsResponse {
    private long todoTasks;
    private long inProgressTasks;
    private long testingTasks;
    private long codeReviewTasks;
    private long doneTasks;
    private long highPriorityTasks;
    private long criticalPriorityTasks;
    private double averageStoryPoints;
    private double completionPercentage;
}
