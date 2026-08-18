package com.neuroforge.backend.pipeline.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PipelineHistoryResponse {

    private Long runId;

    private String pipelineName;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}