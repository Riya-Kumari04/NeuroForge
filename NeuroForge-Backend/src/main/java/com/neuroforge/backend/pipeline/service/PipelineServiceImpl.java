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

import com.neuroforge.backend.pipeline.entity.PipelineStage;
import com.neuroforge.backend.pipeline.repository.PipelineStageRepository;

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
    private final PipelineStageRepository pipelineStageRepository;

    @Override
    public ApiResponse<PipelineRunResponse> runPipeline(RunPipelineRequest request) {

        System.out.println("Received pipelineId = " + request.getPipelineId());
        System.out.println("Exists = " + pipelineRepository.existsById(request.getPipelineId()));
        System.out.println("Pipelines in DB = " + pipelineRepository.findAll());

        Pipeline pipeline = pipelineRepository.findById(request.getPipelineId())
                .orElseThrow(() -> AppException.notFound("Pipeline not found"));

        PipelineRun run = PipelineRun.builder()
                .pipeline(pipeline)
                .status("RUNNING")
                .triggeredBy("PM")   // Temporary value for testing
                .startedAt(LocalDateTime.now())
                .build();

        run = pipelineRunRepository.save(run);
        String[] stages = {
                "Build",
                "Unit Test",
                "Security Scan",
                "Deploy Dev",
                "Deploy QA",
                "Deploy Prod"
        };

        for (int i = 0; i < stages.length; i++) {

            PipelineStage stage = PipelineStage.builder()
                    .pipelineRun(run)
                    .stageName(stages[i])
                    .status("PENDING")
                    .stageOrder(i + 1)
                    .build();

            pipelineStageRepository.save(stage);
        }
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

        PipelineRun run = pipelineRunRepository.findById(runId)
                .orElseThrow(() -> AppException.notFound("Pipeline run not found"));

        List<PipelineStageResponse> stages = pipelineStageRepository.findByPipelineRun(run)
                .stream()
                .map(stage -> PipelineStageResponse.builder()
                        .stageName(stage.getStageName())
                        .status(stage.getStatus())
                        .startedAt(stage.getStartedAt())
                        .completedAt(stage.getCompletedAt())
                        .build())
                .toList();

        return ApiResponse.ok(
                "Pipeline stages fetched successfully",
                stages);
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