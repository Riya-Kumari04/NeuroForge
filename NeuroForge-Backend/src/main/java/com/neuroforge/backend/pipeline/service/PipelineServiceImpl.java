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

import com.neuroforge.backend.project.entity.Task;
import com.neuroforge.backend.project.repository.TaskRepository;

import com.neuroforge.backend.pipeline.repository.PipelineStageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import com.neuroforge.backend.project.repository.TaskRepository;
import com.neuroforge.backend.project.entity.Task;

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
        private final TaskRepository taskRepository;

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

                run = pipelineRunRepository.saveAndFlush(run);

                System.out.println("Saved PipelineRun ID = " + run.getId());
                System.out.println("Exists immediately = " + pipelineRunRepository.existsById(run.getId()));

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
                if (request.getTaskIds() != null) {

                        for (Long taskId : request.getTaskIds()) {

                                ReleaseTask releaseTask = ReleaseTask.builder()
                                                .release(release)
                                                .taskId(taskId)
                                                .build();

                                releaseTaskRepository.save(releaseTask);
                        }
                }

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

                List<ReleaseTask> tasks = releaseTaskRepository.findByReleaseId(releaseId);

                StringBuilder notes = new StringBuilder();

                notes.append("Release Version: ")
                                .append(release.getVersion())
                                .append("\n\nCompleted Tasks:\n");

                if (tasks.isEmpty()) {
                        notes.append("• No tasks linked to this release\n");
                } else {
                        for (ReleaseTask releaseTask : tasks) {

                                Task task = taskRepository
                                                .findById(releaseTask.getTaskId())
                                                .orElse(null);

                                if (task != null) {

                                        notes.append("• ")
                                                        .append(task.getTitle())
                                                        .append("\n");

                                } else {

                                        notes.append("• Task #")
                                                        .append(releaseTask.getTaskId())
                                                        .append("\n");
                                }
                        }
                }

                notes.append("\nStatus: ")
                                .append(release.getStatus());

                release.setReleaseNotes(notes.toString());

                releaseRepository.save(release);

                ReleaseNoteResponse response = ReleaseNoteResponse.builder()
                                .version(release.getVersion())
                                .releaseNotes(release.getReleaseNotes())
                                .build();

                return ApiResponse.ok(
                                "Release notes generated successfully",
                                response);
        }

        @Override
        public ApiResponse<List<PipelineHistoryResponse>> getPipelineHistory() {

                List<PipelineHistoryResponse> history = pipelineRunRepository
                                .findAll()
                                .stream()
                                .map(run -> PipelineHistoryResponse.builder()
                                                .runId(run.getId())
                                                .pipelineName(run.getPipeline().getName())
                                                .status(run.getStatus())
                                                .startedAt(run.getStartedAt())
                                                .completedAt(run.getCompletedAt())
                                                .build())
                                .toList();

                return ApiResponse.ok(
                                "Pipeline history fetched successfully",
                                history);
        }

        @Override
        public ApiResponse<ReleaseNoteResponse> updateReleaseNotes(
                        Long releaseId,
                        UpdateReleaseNotesRequest request) {

                Release release = releaseRepository.findById(releaseId)
                                .orElseThrow(() -> AppException.notFound("Release not found"));

                release.setReleaseNotes(request.getReleaseNotes());

                releaseRepository.save(release);

                ReleaseNoteResponse response = ReleaseNoteResponse.builder()
                                .version(release.getVersion())
                                .releaseNotes(release.getReleaseNotes())
                                .build();

                return ApiResponse.ok(
                                "Release notes updated successfully",
                                response);
        }

        @Override
        public ApiResponse<PipelineRunResponse> retryPipeline(Long runId) {

                PipelineRun oldRun = pipelineRunRepository.findById(runId)
                                .orElseThrow(() -> AppException.notFound("Pipeline run not found"));

                if (!"FAILED".equals(oldRun.getStatus())) {
                        throw AppException.badRequest("Only failed pipelines can be retried");
                }

                PipelineRun newRun = PipelineRun.builder()
                                .pipeline(oldRun.getPipeline())
                                .status("RUNNING")
                                .triggeredBy(oldRun.getTriggeredBy())
                                .startedAt(LocalDateTime.now())
                                .build();

                newRun = pipelineRunRepository.saveAndFlush(newRun);

                pipelineSimulator.simulate(newRun.getId());

                PipelineRunResponse response = PipelineRunResponse.builder()
                                .runId(newRun.getId())
                                .pipelineName(newRun.getPipeline().getName())
                                .status(newRun.getStatus())
                                .startedAt(newRun.getStartedAt())
                                .completedAt(newRun.getCompletedAt())
                                .build();

                return ApiResponse.ok("Pipeline restarted successfully", response);
        }

        @Override
        public ApiResponse<String> approveProduction(Long runId) {

                PipelineRun run = pipelineRunRepository.findById(runId)
                                .orElseThrow(() -> AppException.notFound("Pipeline run not found"));

                if (!"WAITING_FOR_APPROVAL".equals(run.getStatus())) {
                        throw AppException.badRequest("Pipeline is not waiting for approval");
                }

                run.setStatus("APPROVED");
                pipelineRunRepository.save(run);

                pipelineSimulator.deployProduction(runId);

                return ApiResponse.ok(
                                "Production deployment approved",
                                "Deployment started");
        }

        @Override
        public ApiResponse<PipelineMetricsResponse> getPipelineMetrics() {

                long total = pipelineRunRepository.count();
                long success = pipelineRunRepository.countByStatus("SUCCESS");
                long failed = pipelineRunRepository.countByStatus("FAILED");
                long waiting = pipelineRunRepository.countByStatus("WAITING_FOR_APPROVAL");

                double successRate = total == 0 ? 0 : (success * 100.0) / total;

                List<PipelineRun> runs = pipelineRunRepository.findAll();

                double averageDuration = 0;
                long fastest = 0;
                long slowest = 0;

                if (!runs.isEmpty()) {

                        List<Long> durations = runs.stream()
                                        .filter(run -> run.getStartedAt() != null && run.getCompletedAt() != null)
                                        .map(run -> java.time.Duration
                                                        .between(run.getStartedAt(), run.getCompletedAt())
                                                        .getSeconds())
                                        .toList();

                        if (!durations.isEmpty()) {

                                averageDuration = durations.stream()
                                                .mapToLong(Long::longValue)
                                                .average()
                                                .orElse(0);

                                fastest = durations.stream()
                                                .mapToLong(Long::longValue)
                                                .min()
                                                .orElse(0);

                                slowest = durations.stream()
                                                .mapToLong(Long::longValue)
                                                .max()
                                                .orElse(0);
                        }
                }

                PipelineMetricsResponse response = PipelineMetricsResponse.builder()
                                .totalRuns(total)
                                .successfulRuns(success)
                                .failedRuns(failed)
                                .waitingApprovalRuns(waiting)
                                .successRate(successRate)
                                .averageDurationSeconds(averageDuration)
                                .fastestRunSeconds(fastest)
                                .slowestRunSeconds(slowest)
                                .build();

                return ApiResponse.ok(
                                "Pipeline metrics fetched successfully",
                                response);
        }

        @Override
        public ApiResponse<String> cancelPipeline(Long runId) {

                PipelineRun run = pipelineRunRepository.findById(runId)
                                .orElseThrow(() -> AppException.notFound("Pipeline run not found"));

                if (!"RUNNING".equals(run.getStatus())
                                && !"WAITING_FOR_APPROVAL".equals(run.getStatus())) {

                        throw AppException.badRequest("Pipeline cannot be cancelled");
                }

                run.setStatus("CANCELLED");
                run.setCompletedAt(LocalDateTime.now());

                pipelineRunRepository.save(run);

                return ApiResponse.ok(
                                "Pipeline cancelled successfully",
                                "Run ID: " + runId);
        }

        @Override
        public ApiResponse<List<ReleaseHistoryResponse>> getReleaseHistory() {

                List<ReleaseHistoryResponse> releases = releaseRepository.findAll()
                                .stream()
                                .map(release -> ReleaseHistoryResponse.builder()
                                                .id(release.getId())
                                                .version(release.getVersion())
                                                .status(release.getStatus())
                                                .createdAt(release.getCreatedAt())
                                                .releasedAt(release.getReleasedAt())
                                                .build())
                                .toList();

                return ApiResponse.ok(
                                "Release history fetched successfully",
                                releases);
        }

        @Override
        public ApiResponse<ReleaseResponse> publishRelease(Long releaseId) {

                Release release = releaseRepository.findById(releaseId)
                                .orElseThrow(() -> AppException.notFound("Release not found"));

                if ("RELEASED".equals(release.getStatus())) {
                        throw AppException.badRequest("Release is already published");
                }

                release.setStatus("RELEASED");
                release.setReleasedAt(LocalDateTime.now());

                release = releaseRepository.save(release);

                ReleaseResponse response = ReleaseResponse.builder()
                                .id(release.getId())
                                .version(release.getVersion())
                                .status(release.getStatus())
                                .createdAt(release.getCreatedAt())
                                .releasedAt(release.getReleasedAt())
                                .build();

                return ApiResponse.ok(
                                "Release published successfully",
                                response);
        }

}