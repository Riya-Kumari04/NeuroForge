package com.neuroforge.backend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CycleTimePointResponse {

    private UUID taskId;
    private String taskTitle;
    private UUID sprintId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long cycleTimeMinutes;
}
