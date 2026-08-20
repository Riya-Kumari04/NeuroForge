package com.neuroforge.backend.pipeline.controller;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.pipeline.dto.*;
import com.neuroforge.backend.pipeline.service.PipelineService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pipelines")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineService pipelineService;

    @PostMapping("/run")
    public ApiResponse<PipelineRunResponse> runPipeline(
            @RequestBody RunPipelineRequest request) {
        return pipelineService.runPipeline(request);
    }

    @GetMapping("/{runId}/stages")
    public ApiResponse<List<PipelineStageResponse>> getPipelineStages(
            @PathVariable Long runId) {
        return pipelineService.getPipelineStages(runId);
    }

    @GetMapping("/history")
    public ApiResponse<List<PipelineHistoryResponse>> getPipelineHistory() {
        return pipelineService.getPipelineHistory();
    }

    @PostMapping("/release")
    public ApiResponse<ReleaseResponse> createRelease(
            @RequestBody CreateReleaseRequest request) {
        return pipelineService.createRelease(request);
    }

    @GetMapping("/release/{releaseId}/notes")
    public ApiResponse<ReleaseNoteResponse> generateReleaseNotes(
            @PathVariable Long releaseId) {
        return pipelineService.generateReleaseNotes(releaseId);
    }

    @PutMapping("/release/{releaseId}/notes")
    public ApiResponse<ReleaseNoteResponse> updateReleaseNotes(
            @PathVariable Long releaseId,
            @RequestBody UpdateReleaseNotesRequest request) {
        return pipelineService.updateReleaseNotes(releaseId, request);
    }

    @PostMapping("/{runId}/approve")
    public ApiResponse<String> approveProduction(
            @PathVariable Long runId) {
        return pipelineService.approveProduction(runId);
    }

    @GetMapping("/metrics")
    public ApiResponse<PipelineMetricsResponse> getPipelineMetrics() {
        return pipelineService.getPipelineMetrics();
    }

    @PostMapping("/{runId}/retry")
    public ApiResponse<PipelineRunResponse> retryPipeline(
            @PathVariable Long runId) {
        return pipelineService.retryPipeline(runId);
    }

    @PostMapping("/{runId}/cancel")
    public ApiResponse<String> cancelPipeline(
            @PathVariable Long runId) {
        return pipelineService.cancelPipeline(runId);
    }

    @GetMapping("/releases")
    public ApiResponse<List<ReleaseHistoryResponse>> getReleaseHistory() {
        return pipelineService.getReleaseHistory();
    }

    @PostMapping("/release/{releaseId}/publish")
    public ApiResponse<ReleaseResponse> publishRelease(
            @PathVariable Long releaseId) {
        return pipelineService.publishRelease(releaseId);
    }
}
