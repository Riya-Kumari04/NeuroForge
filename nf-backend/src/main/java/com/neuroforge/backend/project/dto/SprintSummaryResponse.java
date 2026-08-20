package com.neuroforge.backend.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintSummaryResponse {
    private Long id;
    private String sprintName;
    private String status;
    private String goal;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;
    private long totalTasks;
    private long completedTasks;
    private long remainingTasks;
    private double completionPercentage;
    private int totalStoryPoints;
    private int completedStoryPoints;
    private int remainingStoryPoints;
    // Module 4: Requirement traceability
    private long tasksWithRequirements;
    private long completedTasksWithRequirements;
}
