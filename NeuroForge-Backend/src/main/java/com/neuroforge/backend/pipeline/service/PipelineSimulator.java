package com.neuroforge.backend.pipeline.service;

import com.neuroforge.backend.pipeline.entity.PipelineRun;
import com.neuroforge.backend.pipeline.entity.PipelineStage;
import com.neuroforge.backend.pipeline.repository.PipelineRunRepository;
import com.neuroforge.backend.pipeline.repository.PipelineStageRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PipelineSimulator {

    private final PipelineRunRepository pipelineRunRepository;
    private final PipelineStageRepository pipelineStageRepository;

    @Async
    @Transactional
    public void simulate(Long runId) {

        System.out.println("===== Pipeline Simulator Started =====");
        System.out.println("Run ID = " + runId);

        PipelineRun run = pipelineRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Pipeline run not found"));

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
                    .stageOrder(i + 1)
                    .status("RUNNING")
                    .startedAt(LocalDateTime.now())
                    .build();

            stage = pipelineStageRepository.save(stage);
            System.out.println("Saved Stage: " + stage.getStageName());
            System.out.println("Stage ID: " + stage.getId());

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Pipeline simulation interrupted", e);
            }

            stage.setStatus("SUCCESS");
            stage.setCompletedAt(LocalDateTime.now());

            pipelineStageRepository.save(stage);
            System.out.println("Completed Stage: " + stage.getStageName());
        }

        run.setStatus("SUCCESS");
        run.setCompletedAt(LocalDateTime.now());

        pipelineRunRepository.save(run);
        System.out.println("Pipeline Simulation Completed");
    }
}