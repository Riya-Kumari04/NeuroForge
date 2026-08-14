package com.neuroforge.backend.pipeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineRunResponse {

    private Long runId;
    private String pipelineName;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
