package com.neuroforge.backend.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintProgressResponse {
    private Long sprintId;
    private String sprintName;
    private long totalTasks;
    private long completedTasks;
    private long remainingTasks;
    private int totalStoryPoints;
    private int completedStoryPoints;
    private int remainingStoryPoints;
    private double completionPercentage;
    private String currentSprintStatus;
}
