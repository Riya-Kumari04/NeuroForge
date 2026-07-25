package com.neuroforge.backend.dto;

import com.neuroforge.backend.enums.SprintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintSummaryResponse {
    private UUID id;
    private String name;
    private SprintStatus status;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private long totalTasks;
    private long completedTasks;
    private long remainingTasks;
    private double completionPercentage;
    private int totalStoryPoints;
    private int completedStoryPoints;
    private int remainingStoryPoints;
}
