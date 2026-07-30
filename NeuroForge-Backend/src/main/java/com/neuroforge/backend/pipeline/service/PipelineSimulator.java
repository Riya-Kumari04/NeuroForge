package com.neuroforge.backend.pipeline.service;

import com.neuroforge.backend.pipeline.entity.PipelineRun;
import com.neuroforge.backend.pipeline.entity.PipelineStage;
import com.neuroforge.backend.pipeline.repository.PipelineRunRepository;
import com.neuroforge.backend.pipeline.repository.PipelineStageRepository;
import java.util.Random;
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
    private final Random random = new Random();

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
                "Deploy QA"
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

            boolean failed = random.nextInt(100) < 20; // 20% chance

            if (failed) {

                stage.setStatus("FAILED");
                stage.setCompletedAt(LocalDateTime.now());

                pipelineStageRepository.save(stage);

                run.setStatus("FAILED");
                run.setCompletedAt(LocalDateTime.now());

                pipelineRunRepository.save(run);

                System.out.println("Pipeline Failed at Stage: " + stage.getStageName());

                return;
            }

            stage.setStatus("SUCCESS");
            stage.setCompletedAt(LocalDateTime.now());

            pipelineStageRepository.save(stage);

            System.out.println("Completed Stage: " + stage.getStageName());
        }

        run.setStatus("WAITING_FOR_APPROVAL");
        pipelineRunRepository.save(run);

        System.out.println("Waiting for PM approval before Production Deployment");
    }

    @Async
    @Transactional
    public void deployProduction(Long runId) {

        PipelineRun run = pipelineRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Pipeline run not found"));

        PipelineStage stage = PipelineStage.builder()
                .pipelineRun(run)
                .stageName("Deploy Prod")
                .stageOrder(6)
                .status("RUNNING")
                .startedAt(LocalDateTime.now())
                .build();

        stage = pipelineStageRepository.save(stage);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Production deployment interrupted", e);
        }

        boolean failed = random.nextInt(100) < 10; // 10% chance

        if (failed) {

            stage.setStatus("FAILED");
            stage.setCompletedAt(LocalDateTime.now());

            pipelineStageRepository.save(stage);

            run.setStatus("FAILED");
            run.setCompletedAt(LocalDateTime.now());

            pipelineRunRepository.save(run);

            System.out.println("Production Deployment Failed");

            return;
        }

        stage.setStatus("SUCCESS");
        stage.setCompletedAt(LocalDateTime.now());

        pipelineStageRepository.save(stage);

        run.setStatus("SUCCESS");
        run.setCompletedAt(LocalDateTime.now());

        pipelineRunRepository.save(run);

        System.out.println("Production deployment completed");
    }
}