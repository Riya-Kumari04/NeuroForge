package com.neuroforge.backend.pipeline.service;

import com.neuroforge.backend.ai.dto.ReleaseNotesRequest;
import com.neuroforge.backend.ai.dto.ReleaseNotesResponse;
import com.neuroforge.backend.ai.service.GroqService;
import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.pipeline.dto.*;
import com.neuroforge.backend.pipeline.entity.Pipeline;
import com.neuroforge.backend.pipeline.entity.PipelineRun;
import com.neuroforge.backend.pipeline.entity.Release;
import com.neuroforge.backend.pipeline.entity.ReleaseTask;
import com.neuroforge.backend.pipeline.repository.PipelineRepository;
import com.neuroforge.backend.pipeline.repository.PipelineRunRepository;
import com.neuroforge.backend.pipeline.repository.ReleaseRepository;
import com.neuroforge.backend.pipeline.repository.ReleaseTaskRepository;
import lombok.RequiredArgsConstructor;

import com.neuroforge.backend.project.entity.Task;
import com.neuroforge.backend.project.repository.TaskRepository;

import com.neuroforge.backend.pipeline.repository.PipelineStageRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PipelineServiceImpl implements PipelineService {
    private final PipelineRepository pipelineRepository;
    private final PipelineRunRepository pipelineRunRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final PipelineSimulator pipelineSimulator;
    private final ReleaseRepository releaseRepository;
    private final ReleaseTaskRepository releaseTaskRepository;
    private final TaskRepository taskRepository;
    private final GroqService groqService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ApiResponse<PipelineRunResponse> runPipeline(RunPipelineRequest request) {
        Pipeline pipeline = pipelineRepository.findById(request.getPipelineId())
                .orElseThrow(() -> AppException.notFound("Pipeline not found"));

        PipelineRun run = PipelineRun.builder()
                .pipeline(pipeline)
                .status("RUNNING")
                .triggeredBy(getCurrentUserEmail())
                .startedAt(LocalDateTime.now())
                .build();

        run = pipelineRunRepository.saveAndFlush(run);

        PipelineRunResponse response = PipelineRunResponse.builder()
                .runId(run.getId())
                .pipelineName(pipeline.getName())
                .status(run.getStatus())
                .startedAt(run.getStartedAt())
                .completedAt(run.getCompletedAt())
                .build();

        // Call simulator after transaction commits
        pipelineSimulator.simulate(run.getId());

        return ApiResponse.ok(
                "Pipeline started successfully",
                response);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional
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
    @Transactional
    public ApiResponse<ReleaseNoteResponse> generateReleaseNotes(Long releaseId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> AppException.notFound("Release not found"));

        List<ReleaseTask> releaseTasks = releaseTaskRepository.findByReleaseId(releaseId);

        List<String> doneTaskTitles = releaseTasks.stream()
                .map(releaseTask -> {
                    Task task = taskRepository.findById(releaseTask.getTaskId()).orElse(null);
                    if (task != null && "DONE".equals(task.getStatus())) {
                        return task.getTitle();
                    }
                    return null;
                })
                .filter(title -> title != null)
                .collect(Collectors.toList());

        ReleaseNotesResponse llmResponse;
        if (doneTaskTitles.isEmpty()) {
            llmResponse = ReleaseNotesResponse.builder()
                    .releaseNotes("No completed tasks found for this release.")
                    .build();
        } else {
            ReleaseNotesRequest llmRequest = ReleaseNotesRequest.builder()
                    .tasks(doneTaskTitles)
                    .build();
            llmResponse = groqService.generateReleaseNotes(llmRequest);
        }

        release.setReleaseNotes(llmResponse.getReleaseNotes());
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
    @Transactional(readOnly = true)
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
    @Transactional
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
    @Transactional
    public ApiResponse<PipelineRunResponse> retryPipeline(Long runId) {
        PipelineRun oldRun = pipelineRunRepository.findById(runId)
                .orElseThrow(() -> AppException.notFound("Pipeline run not found"));

        if (!"FAILED".equals(oldRun.getStatus())) {
                throw AppException.badRequest("Only failed pipelines can be retried");
        }

        PipelineRun newRun = PipelineRun.builder()
                .pipeline(oldRun.getPipeline())
                .status("RUNNING")
                .triggeredBy(getCurrentUserEmail())
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
    @Transactional
    public ApiResponse<String> approveProduction(Long runId) {
        if (!hasRole("ROLE_PROJECT_MANAGER")) {
                throw AppException.forbidden("Only Project Managers can approve production deployment");
        }

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
    @Transactional(readOnly = true)
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
    @Transactional
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
    @Transactional(readOnly = true)
    public ApiResponse<List<ReleaseHistoryResponse>> getReleaseHistory() {
        List<ReleaseHistoryResponse> releases = releaseRepository.findAll()
                        .stream()
                        .map(release -> ReleaseHistoryResponse.builder()
                                        .id(release.getId())
                                        .version(release.getVersion())
                                        .status(release.getStatus())
                                        .createdAt(release.getCreatedAt())
                                        .releasedAt(release.getReleasedAt())
                                        .releaseNotes(release.getReleaseNotes())
                                        .build())
                        .toList();

        return ApiResponse.ok(
                "Release history fetched successfully",
                releases);
    }

    @Override
    @Transactional
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

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
        }
        return "system";
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getAuthorities().stream()
                                .anyMatch(authority -> authority.getAuthority().equals(role));
        }
        return false;
    }
}
