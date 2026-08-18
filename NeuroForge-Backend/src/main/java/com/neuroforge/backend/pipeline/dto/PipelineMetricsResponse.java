package com.neuroforge.backend.pipeline.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PipelineMetricsResponse {

    private long totalRuns;

    private long successfulRuns;

    private long failedRuns;

    private long waitingApprovalRuns;

    private double successRate;

    private double averageDurationSeconds;

    private long fastestRunSeconds;

    private long slowestRunSeconds;
}