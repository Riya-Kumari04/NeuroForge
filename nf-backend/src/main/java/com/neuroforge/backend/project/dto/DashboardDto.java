package com.neuroforge.backend.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    private long totalProjects;

    private long activeProjects;

    private long completedProjects;

    private long totalSprints;

    private long totalTasks;

    private long completedTasks;

    private long pendingTasks;

    private double overallProgress;
}