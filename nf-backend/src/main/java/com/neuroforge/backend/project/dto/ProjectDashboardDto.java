package com.neuroforge.backend.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectDashboardDto {

    private Long projectId;

    private String projectName;

    private long totalTasks;

    private long completedTasks;

    private long pendingTasks;

    private long inProgressTasks;

    private long totalMembers;

    private long totalSprints;

    private double progress;
}