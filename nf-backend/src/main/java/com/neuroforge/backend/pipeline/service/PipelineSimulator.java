package com.neuroforge.backend.pipeline.service;

import com.neuroforge.backend.pipeline.entity.PipelineRun;
import com.neuroforge.backend.pipeline.entity.PipelineStage;
import com.neuroforge.backend.pipeline.repository.PipelineRunRepository;
import com.neuroforge.backend.pipeline.repository.PipelineStageRepository;
import com.neuroforge.backend.pipeline.dto.PipelineStageUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineSimulator {

    private final PipelineRunRepository pipelineRunRepository;
    private final PipelineStageRepository pipelineStageRepository;
    @Value("${pipeline.failure-rate:20}")
    private int failureRate;

    @Value("${pipeline.production-failure-rate:10}")
    private int productionFailureRate;

    private final PipelineWebSocketService pipelineWebSocketService;
    private final Random random = new Random();

    @Async
    public void simulate(Long runId) {
        log.info("===== Pipeline Simulator Started =====");
        log.info("Run ID = {}", runId);

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
            run = pipelineRunRepository.findById(run.getId())
                    .orElseThrow(() -> new RuntimeException("Pipeline run not found"));

            if ("CANCELLED".equals(run.getStatus())) {
                log.info("Pipeline {} cancelled.", run.getId());
                return;
            }

            PipelineStage stage = PipelineStage.builder()
                    .pipelineRun(run)
                    .stageName(stages[i])
                    .stageOrder(i + 1)
                    .status("RUNNING")
                    .startedAt(LocalDateTime.now())
                    .build();

            stage = pipelineStageRepository.save(stage);
            pipelineWebSocketService.publish(
                    PipelineStageUpdate.builder()
                            .runId(run.getId())
                            .stageName(stage.getStageName())
                            .status(stage.getStatus())
                            .build());
            log.info("Saved Stage: {}", stage.getStageName());

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Pipeline simulation interrupted", e);
            }

            boolean failed = random.nextInt(100) < failureRate;

            if (failed) {
                stage.setStatus("FAILED");
                stage.setCompletedAt(LocalDateTime.now());

                pipelineStageRepository.save(stage);
                pipelineWebSocketService.publish(
                        PipelineStageUpdate.builder()
                                .runId(run.getId())
                                .stageName(stage.getStageName())
                                .status("FAILED")
                                .build());

                run.setStatus("FAILED");
                run.setCompletedAt(LocalDateTime.now());

                pipelineRunRepository.save(run);

                log.error("Pipeline Failed at Stage: {}", stage.getStageName());

                return;
            }

            stage.setStatus("SUCCESS");
            stage.setCompletedAt(LocalDateTime.now());

            pipelineStageRepository.save(stage);
            pipelineWebSocketService.publish(
                    PipelineStageUpdate.builder()
                            .runId(run.getId())
                            .stageName(stage.getStageName())
                            .status("SUCCESS")
                            .build());

            log.info("Completed Stage: {}", stage.getStageName());
        }

        run.setStatus("WAITING_FOR_APPROVAL");
        pipelineRunRepository.save(run);

        log.info("Waiting for PM approval before Production Deployment");
    }

    @Async
    public void deployProduction(Long runId) {
        PipelineRun run = pipelineRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Pipeline run not found"));

        if ("CANCELLED".equals(run.getStatus())) {
            return;
        }

        PipelineStage stage = PipelineStage.builder()
                .pipelineRun(run)
                .stageName("Deploy Prod")
                .stageOrder(6)
                .status("RUNNING")
                .startedAt(LocalDateTime.now())
                .build();

        stage = pipelineStageRepository.save(stage);
        pipelineWebSocketService.publish(
                PipelineStageUpdate.builder()
                        .runId(run.getId())
                        .stageName(stage.getStageName())
                        .status("RUNNING")
                        .build());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Production deployment interrupted", e);
        }

        boolean failed = random.nextInt(100) < productionFailureRate;

        if (failed) {
            stage.setStatus("FAILED");
            stage.setCompletedAt(LocalDateTime.now());

            pipelineStageRepository.save(stage);
            pipelineWebSocketService.publish(
                    PipelineStageUpdate.builder()
                            .runId(run.getId())
                            .stageName(stage.getStageName())
                            .status("FAILED")
                            .build());

            run.setStatus("FAILED");
            run.setCompletedAt(LocalDateTime.now());

            pipelineRunRepository.save(run);

            log.error("Production Deployment Failed");

            return;
        }

        stage.setStatus("SUCCESS");
        stage.setCompletedAt(LocalDateTime.now());

        pipelineStageRepository.save(stage);
        pipelineWebSocketService.publish(
                PipelineStageUpdate.builder()
                        .runId(run.getId())
                        .stageName(stage.getStageName())
                        .status("SUCCESS")
                        .build());

        run.setStatus("SUCCESS");
        run.setCompletedAt(LocalDateTime.now());

        pipelineRunRepository.save(run);

        log.info("Production deployment completed");
    }
}
