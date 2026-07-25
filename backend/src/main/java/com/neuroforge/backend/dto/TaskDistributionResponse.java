package com.neuroforge.backend.dto;

import com.neuroforge.backend.enums.TaskPriority;
import com.neuroforge.backend.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDistributionResponse {
    private Map<TaskStatus, Long> byStatus;
    private Map<TaskPriority, Long> byPriority;
    private Map<String, Long> byAssignee;
}
