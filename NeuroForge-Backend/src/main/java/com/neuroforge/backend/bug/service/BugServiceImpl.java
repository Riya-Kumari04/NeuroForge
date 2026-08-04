package com.neuroforge.backend.bug.service;

import com.neuroforge.backend.ai.service.GroqService;
import com.neuroforge.backend.bug.dto.BugResponse;
import com.neuroforge.backend.bug.dto.CreateBugRequest;
import com.neuroforge.backend.bug.dto.DuplicateCheckResponse;
import com.neuroforge.backend.bug.entity.Bug;
import com.neuroforge.backend.bug.repository.BugRepository;
import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.bug.repository.BugStatusHistoryRepository;
import com.neuroforge.backend.bug.entity.BugStatusHistory;
import com.neuroforge.backend.bug.dto.UpdateBugStatusRequest;
import com.neuroforge.backend.bug.dto.IncidentAlert;
// import com.neuroforge.backend.bug.dto.DuplicateCheckResponse;
// import com.neuroforge.backend.bug.dto.CreateBugRequest;
import lombok.RequiredArgsConstructor;
import com.neuroforge.backend.bug.dto.SlaTimerResponse;

import com.neuroforge.backend.bug.dto.IncidentResponse;
import com.neuroforge.backend.ai.service.GroqService;
import com.neuroforge.backend.bug.entity.Incident;
import com.neuroforge.backend.bug.repository.IncidentRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BugServiceImpl implements BugService {

        private final BugRepository bugRepository;
        private final BugStatusHistoryRepository historyRepository;
        private final IncidentRepository incidentRepository;
        private final BugWebSocketService bugWebSocketService;
        private final GroqService groqService;

        @Override
        public ApiResponse<BugResponse> createBug(CreateBugRequest request) {

                Bug bug = Bug.builder()
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .severity(request.getSeverity())
                                .environment(request.getEnvironment())
                                .attachmentUrl(request.getAttachmentUrl())
                                .status("OPEN")
                                .build();

                bug = bugRepository.save(bug);
                if ("CRITICAL".equalsIgnoreCase(bug.getSeverity())) {

                        Incident incident = Incident.builder()
                                        .bug(bug)
                                        .status("ACTIVE")
                                        .build();

                        incident = incidentRepository.save(incident);

                        bugWebSocketService.publish(
                                        IncidentAlert.builder()
                                                        .incidentId(incident.getId())
                                                        .bugId(bug.getId())
                                                        .title(bug.getTitle())
                                                        .severity(bug.getSeverity())
                                                        .status("ACTIVE")
                                                        .build());
                }

                BugResponse response = BugResponse.builder()
                                .id(bug.getId())
                                .title(bug.getTitle())
                                .description(bug.getDescription())
                                .severity(bug.getSeverity())
                                .status(bug.getStatus())
                                .environment(bug.getEnvironment())
                                .attachmentUrl(bug.getAttachmentUrl())
                                .createdAt(bug.getCreatedAt())
                                .updatedAt(bug.getUpdatedAt())
                                .build();

                return ApiResponse.ok("Bug created successfully", response);
        }

        @Override
        public ApiResponse<List<BugResponse>> getAllBugs() {

                List<BugResponse> bugs = bugRepository.findAll()
                                .stream()
                                .map(bug -> BugResponse.builder()
                                                .id(bug.getId())
                                                .title(bug.getTitle())
                                                .description(bug.getDescription())
                                                .severity(bug.getSeverity())
                                                .status(bug.getStatus())
                                                .environment(bug.getEnvironment())
                                                .attachmentUrl(bug.getAttachmentUrl())
                                                .createdAt(bug.getCreatedAt())
                                                .updatedAt(bug.getUpdatedAt())
                                                .build())
                                .toList();

                return ApiResponse.ok(
                                "Bugs fetched successfully",
                                bugs);
        }

        @Override
        public ApiResponse<BugResponse> getBugById(Long bugId) {

                Bug bug = bugRepository.findById(bugId)
                                .orElseThrow(() -> AppException.notFound("Bug not found"));

                BugResponse response = BugResponse.builder()
                                .id(bug.getId())
                                .title(bug.getTitle())
                                .description(bug.getDescription())
                                .severity(bug.getSeverity())
                                .status(bug.getStatus())
                                .environment(bug.getEnvironment())
                                .attachmentUrl(bug.getAttachmentUrl())
                                .createdAt(bug.getCreatedAt())
                                .updatedAt(bug.getUpdatedAt())
                                .build();

                return ApiResponse.ok(
                                "Bug fetched successfully",
                                response);
        }

        @Override
        public ApiResponse<BugResponse> updateBugStatus(
                        Long bugId,
                        UpdateBugStatusRequest request) {

                Bug bug = bugRepository.findById(bugId)
                                .orElseThrow(() -> AppException.notFound("Bug not found"));

                String oldStatus = bug.getStatus();

                bug.setStatus(request.getStatus());

                bug = bugRepository.save(bug);

                BugStatusHistory history = BugStatusHistory.builder()
                                .bug(bug)
                                .oldStatus(oldStatus)
                                .newStatus(request.getStatus())
                                .changedBy("SYSTEM")
                                .build();

                historyRepository.save(history);

                BugResponse response = BugResponse.builder()
                                .id(bug.getId())
                                .title(bug.getTitle())
                                .description(bug.getDescription())
                                .severity(bug.getSeverity())
                                .status(bug.getStatus())
                                .environment(bug.getEnvironment())
                                .attachmentUrl(bug.getAttachmentUrl())
                                .createdAt(bug.getCreatedAt())
                                .updatedAt(bug.getUpdatedAt())
                                .build();

                return ApiResponse.ok(
                                "Bug status updated successfully",
                                response);
        }

        @Override
        public ApiResponse<List<IncidentResponse>> getAllIncidents() {

                List<IncidentResponse> incidents = incidentRepository.findAll()
                                .stream()
                                .map(incident -> IncidentResponse.builder()
                                                .id(incident.getId())
                                                .bugId(incident.getBug().getId())
                                                .bugTitle(incident.getBug().getTitle())
                                                .status(incident.getStatus())
                                                .startedAt(incident.getStartedAt())
                                                .resolvedAt(incident.getResolvedAt())
                                                .build())
                                .toList();

                return ApiResponse.ok(
                                "Incidents fetched successfully",
                                incidents);
        }

        @Override
        public ApiResponse<SlaTimerResponse> getSlaTimer(Long incidentId) {

                Incident incident = incidentRepository.findById(incidentId)
                                .orElseThrow(() -> AppException.notFound("Incident not found"));

                Duration duration;

                if (incident.getResolvedAt() == null) {
                        duration = Duration.between(
                                        incident.getStartedAt(),
                                        LocalDateTime.now());
                } else {
                        duration = Duration.between(
                                        incident.getStartedAt(),
                                        incident.getResolvedAt());
                }

                long seconds = duration.getSeconds();

                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long remainingSeconds = seconds % 60;

                String formatted = String.format(
                                "%02dh %02dm %02ds",
                                hours,
                                minutes,
                                remainingSeconds);

                SlaTimerResponse response = SlaTimerResponse.builder()
                                .incidentId(incident.getId())
                                .status(incident.getStatus())
                                .elapsedSeconds(seconds)
                                .elapsedTime(formatted)
                                .build();

                return ApiResponse.ok(
                                "SLA timer fetched successfully",
                                response);
        }

        @Override
        public ApiResponse<IncidentResponse> resolveIncident(Long incidentId) {

                Incident incident = incidentRepository.findById(incidentId)
                                .orElseThrow(() -> AppException.notFound("Incident not found"));

                incident.setStatus("RESOLVED");
                incident.setResolvedAt(LocalDateTime.now());

                incident = incidentRepository.save(incident);

                bugWebSocketService.publish(
                                IncidentAlert.builder()
                                                .incidentId(incident.getId())
                                                .bugId(incident.getBug().getId())
                                                .title(incident.getBug().getTitle())
                                                .severity(incident.getBug().getSeverity())
                                                .status("RESOLVED")
                                                .build());

                IncidentResponse response = IncidentResponse.builder()
                                .id(incident.getId())
                                .bugId(incident.getBug().getId())
                                .bugTitle(incident.getBug().getTitle())
                                .status(incident.getStatus())
                                .startedAt(incident.getStartedAt())
                                .resolvedAt(incident.getResolvedAt())
                                .build();

                return ApiResponse.ok(
                                "Incident resolved successfully",
                                response);
        }

        @Override
        public ApiResponse<DuplicateCheckResponse> checkDuplicate(CreateBugRequest request) {

                List<Bug> openBugs = bugRepository.findByStatus("OPEN");

                for (Bug bug : openBugs) {

                        boolean duplicate = groqService.isDuplicate(
                                        bug.getTitle() + "\n" + bug.getDescription(),
                                        request.getTitle() + "\n" + request.getDescription());

                        if (duplicate) {

                                DuplicateCheckResponse response = DuplicateCheckResponse.builder()
                                                .duplicate(true)
                                                .duplicateBugId(bug.getId())
                                                .message("Possible duplicate of BUG-" + bug.getId())
                                                .build();

                                return ApiResponse.ok(
                                                "Duplicate check completed",
                                                response);
                        }
                }

                DuplicateCheckResponse response = DuplicateCheckResponse.builder()
                                .duplicate(false)
                                .duplicateBugId(null)
                                .message("No duplicate bug found.")
                                .build();

                return ApiResponse.ok(
                                "Duplicate check completed",
                                response);
        }
}