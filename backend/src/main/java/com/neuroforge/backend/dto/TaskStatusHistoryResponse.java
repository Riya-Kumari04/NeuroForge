package com.neuroforge.backend.dto;

import com.neuroforge.backend.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusHistoryResponse {
    private UUID id;
    private UUID taskId;
    private TaskStatus previousStatus;
    private TaskStatus newStatus;
    private String changedBy;
    private LocalDateTime changedAt;
}
