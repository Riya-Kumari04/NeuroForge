package com.neuroforge.backend.pipeline.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.pipeline.dto.*;
import com.neuroforge.backend.pipeline.entity.Pipeline;
import com.neuroforge.backend.pipeline.entity.PipelineRun;
import com.neuroforge.backend.pipeline.repository.PipelineRepository;
import com.neuroforge.backend.pipeline.repository.PipelineRunRepository;
import com.neuroforge.backend.pipeline.entity.Release;
import com.neuroforge.backend.pipeline.entity.ReleaseTask;
import com.neuroforge.backend.pipeline.repository.ReleaseRepository;
import com.neuroforge.backend.pipeline.repository.ReleaseTaskRepository;
import lombok.RequiredArgsConstructor;
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
        private final PipelineSimulator pipelineSimulator;
        private final ReleaseRepository releaseRepository;
        private final ReleaseTaskRepository releaseTaskRepository;

        @Override
        public ApiResponse<PipelineRunResponse> runPipeline(RunPipelineRequest request) {

                Pipeline pipeline = pipelineRepository.findById(request.getPipelineId())
                                .orElseThrow(() -> AppException.notFound("Pipeline not found"));

                PipelineRun run = PipelineRun.builder()
                                .pipeline(pipeline)
                                .status("RUNNING")
                                .triggeredBy("PM") // Temporary value until authentication is integrated
                                .startedAt(LocalDateTime.now())
                                .build();

                run = pipelineRunRepository.save(run);
                run = pipelineRunRepository.saveAndFlush(run);

                System.out.println("Saved PipelineRun ID = " + run.getId());
                System.out.println("Exists immediately = " + pipelineRunRepository.existsById(run.getId()));

                // Start pipeline simulation asynchronously
                pipelineSimulator.simulate(run.getId());

                // Start pipeline simulation asynchronously
                pipelineSimulator.simulate(run.getId());

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

                Release release = Release.builder()
                                .version(request.getVersion())
                                .status("DRAFT")
                                .createdAt(LocalDateTime.now())
                                .build();

                release = releaseRepository.save(release);

                ReleaseResponse response = ReleaseResponse.builder()
                                .id(release.getId())
                                .version(release.getVersion())
                                .status(release.getStatus())
                                .createdAt(release.getCreatedAt())
                                .releasedAt(release.getReleasedAt())
                                .build();

                return ApiResponse.ok(
                                "Release created successfully",
                                response);
        }

        @Override
        public ApiResponse<ReleaseNoteResponse> generateReleaseNotes(Long releaseId) {

                Release release = releaseRepository.findById(releaseId)
                                .orElseThrow(() -> AppException.notFound("Release not found"));

                String notes = """
                                Release Version: %s

                                Changes Included:
                                • CI/CD Pipeline simulation completed
                                • Pipeline stage tracking implemented
                                • Asynchronous pipeline execution added
                                • Release tracking API implemented

                                Status: %s
                                """.formatted(
                                release.getVersion(),
                                release.getStatus());

                release.setReleaseNotes(notes);
                releaseRepository.save(release);

                ReleaseNoteResponse response = ReleaseNoteResponse.builder()
                                .version(release.getVersion())
                                .releaseNotes(release.getReleaseNotes())
                                .build();

                return ApiResponse.ok(
                                "Release notes generated successfully",
                                response);
        }

}