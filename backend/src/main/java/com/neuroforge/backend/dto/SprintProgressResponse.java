package com.neuroforge.backend.dto;

import com.neuroforge.backend.enums.SprintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintProgressResponse {
    private UUID sprintId;
    private String sprintName;
    private long totalTasks;
    private long completedTasks;
    private long remainingTasks;
    private int totalStoryPoints;
    private int completedStoryPoints;
    private int remainingStoryPoints;
    private double completionPercentage;
    private SprintStatus currentSprintStatus;
}
