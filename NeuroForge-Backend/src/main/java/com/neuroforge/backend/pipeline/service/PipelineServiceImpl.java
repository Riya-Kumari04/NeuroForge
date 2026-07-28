package com.neuroforge.backend.pipeline.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.pipeline.dto.*;
import com.neuroforge.backend.pipeline.entity.Pipeline;
import com.neuroforge.backend.pipeline.entity.PipelineRun;
import com.neuroforge.backend.pipeline.repository.PipelineRepository;
import com.neuroforge.backend.pipeline.repository.PipelineRunRepository;
import org.springframework.scheduling.annotation.Async;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PipelineServiceImpl implements PipelineService {
    private final PipelineRepository pipelineRepository;
    private final PipelineRunRepository pipelineRunRepository;

    @Override
    public ApiResponse<PipelineRunResponse> runPipeline(RunPipelineRequest request) {

        Pipeline pipeline = pipelineRepository.findById(request.getPipelineId())
                .orElseThrow(() -> AppException.notFound("Pipeline not found"));

        PipelineRun run = PipelineRun.builder()
                .pipeline(pipeline)
                .status("RUNNING")
                .startedAt(LocalDateTime.now())
                .build();

        run = pipelineRunRepository.save(run);
        simulatePipeline(run.getId());

        PipelineRunResponse response = PipelineRunResponse.builder()
                .runId(run.getId())
                .pipelineName(pipeline.getName())
                .status(run.getStatus())
                .startedAt(run.getStartedAt())
                .completedAt(run.getCompletedAt())
                .build();

        return ApiResponse.ok(
                "Pipeline started successfully",
                response);
    }

    @Override
    public ApiResponse<List<PipelineStageResponse>> getPipelineStages(Long runId) {
        return null;
    }

    @Override
    public ApiResponse<ReleaseResponse> createRelease(CreateReleaseRequest request) {
        return null;
    }

    @Override
    public ApiResponse<ReleaseNoteResponse> generateReleaseNotes(Long releaseId) {
        return null;
    }

    @Async
public void simulatePipeline(Long runId) {

    try {

        String[] stages = {
                "Build",
                "Unit Test",
                "Security Scan",
                "Deploy Dev",
                "Deploy QA",
                "Deploy Prod"
        };

        for (String stage : stages) {

            System.out.println("Running Stage : " + stage);

            Thread.sleep(3000);

            System.out.println(stage + " completed");
        }

        PipelineRun run = pipelineRunRepository.findById(runId)
                .orElseThrow(() -> AppException.notFound("Pipeline run not found"));

        run.setStatus("SUCCESS");
        run.setCompletedAt(LocalDateTime.now());

        pipelineRunRepository.save(run);

        System.out.println("Pipeline Finished");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}