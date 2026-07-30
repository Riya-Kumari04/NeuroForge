package com.neuroforge.backend.pipeline.controller;

// import com.neuroforge.backend.controller.RestController;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/{runId}/approve")
    public ApiResponse<String> approveProduction(
            @PathVariable Long runId) {

        return pipelineService.approveProduction(runId);
    }
}