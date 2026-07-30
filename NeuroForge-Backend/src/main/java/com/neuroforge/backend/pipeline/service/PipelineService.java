package com.neuroforge.backend.pipeline.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.pipeline.dto.*;
import java.util.List;

public interface PipelineService {

    ApiResponse<PipelineRunResponse> runPipeline(RunPipelineRequest request);

    ApiResponse<List<PipelineStageResponse>> getPipelineStages(Long runId);

    ApiResponse<ReleaseResponse> createRelease(CreateReleaseRequest request);

    ApiResponse<ReleaseNoteResponse> generateReleaseNotes(Long releaseId);

    ApiResponse<String> approveProduction(Long runId);
}