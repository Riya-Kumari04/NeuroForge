package com.neuroforge.backend.project.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatsDto {

    private Long projectId;
    private String projectName;
    private String status;
    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long todoTasks;
    private long totalSprints;
    private long totalMembers;
    private int healthScore;         // 0-100
    private String healthStatus;     // HEALTHY | AT_RISK | CRITICAL
}
