package com.neuroforge.backend.pipeline.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.pipeline.dto.*;

import java.util.List;

public interface PipelineService {

    ApiResponse<PipelineRunResponse> runPipeline(RunPipelineRequest request);

    ApiResponse<List<PipelineStageResponse>> getPipelineStages(Long runId);

    ApiResponse<PipelineMetricsResponse> getPipelineMetrics();

    ApiResponse<List<PipelineHistoryResponse>> getPipelineHistory();

    ApiResponse<PipelineRunResponse> retryPipeline(Long runId);

    ApiResponse<String> cancelPipeline(Long runId);

    ApiResponse<ReleaseResponse> createRelease(CreateReleaseRequest request);

    ApiResponse<ReleaseNoteResponse> generateReleaseNotes(Long releaseId);

    ApiResponse<ReleaseNoteResponse> updateReleaseNotes(
            Long releaseId,
            UpdateReleaseNotesRequest request);

    ApiResponse<String> approveProduction(Long runId);

    ApiResponse<List<ReleaseHistoryResponse>> getReleaseHistory();

    ApiResponse<ReleaseResponse> publishRelease(Long releaseId);
}
