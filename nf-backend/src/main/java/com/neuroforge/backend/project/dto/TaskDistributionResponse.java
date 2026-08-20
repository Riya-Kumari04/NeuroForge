package com.neuroforge.backend.project.dto;

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
    private Map<String, Long> byStatus;
    private Map<String, Long> byPriority;
    private Map<String, Long> byAssignee;
}
