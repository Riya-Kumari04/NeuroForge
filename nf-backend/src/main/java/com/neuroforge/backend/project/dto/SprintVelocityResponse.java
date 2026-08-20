package com.neuroforge.backend.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintVelocityResponse {
    private int completedStoryPoints;
    private long completedTasks;
    private double averageStoryPointsPerTask;
    private double completionPercentage;
}
