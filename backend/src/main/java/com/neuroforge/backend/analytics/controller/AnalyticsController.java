package com.neuroforge.backend.analytics.controller;

import com.neuroforge.backend.analytics.dto.AnalyticsDashboardResponse;
import com.neuroforge.backend.analytics.dto.BurndownResponse;
import com.neuroforge.backend.analytics.dto.ChangeFailureRateResponse;
import com.neuroforge.backend.analytics.dto.CycleTimeResponse;
import com.neuroforge.backend.analytics.dto.DeveloperAnalyticsResponse;
import com.neuroforge.backend.analytics.dto.DeploymentFrequencyResponse;
import com.neuroforge.backend.analytics.dto.IssueTrendResponse;
import com.neuroforge.backend.analytics.dto.MetricsSnapshotResponse;
import com.neuroforge.backend.analytics.dto.SprintAnalyticsResponse;
import com.neuroforge.backend.analytics.dto.SprintHealthSummaryResponse;
import com.neuroforge.backend.analytics.dto.TaskDistributionResponse;
import com.neuroforge.backend.analytics.dto.VelocityHistoryResponse;
import com.neuroforge.backend.analytics.dto.VelocityResponse;
import com.neuroforge.backend.analytics.service.AnalyticsService;
import com.neuroforge.backend.analytics.service.MetricsSnapshotService;
import com.neuroforge.backend.analytics.service.SprintHealthSummaryService;
import com.neuroforge.backend.analytics.service.SprintReportPdfService;
import com.neuroforge.backend.analytics.service.VelocityHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final MetricsSnapshotService metricsSnapshotService;
    private final VelocityHistoryService velocityHistoryService;
    private final SprintHealthSummaryService sprintHealthSummaryService;
    private final SprintReportPdfService sprintReportPdfService;

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDashboardResponse> getDashboard() {
        // TODO Implement analytics dashboard endpoint
        return ResponseEntity.ok(analyticsService.getDashboard());
    }

    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<SprintAnalyticsResponse> getSprintAnalytics(@PathVariable UUID sprintId) {
        // TODO Implement sprint analytics endpoint
        return ResponseEntity.ok(analyticsService.getSprintAnalytics(sprintId));
    }

    @GetMapping("/sprint/{sprintId}/health-summary")
    public ResponseEntity<SprintHealthSummaryResponse> getSprintHealthSummary(@PathVariable UUID sprintId) {
        return ResponseEntity.ok(sprintHealthSummaryService.generateSummary(sprintId));
    }

    @GetMapping(value = "/reports/sprint/{sprintId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getSprintReportPdf(@PathVariable UUID sprintId) {
        byte[] pdfBytes = sprintReportPdfService.generateSprintReportPdf(sprintId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("sprint-analytics-" + sprintId + ".pdf")
                .build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/developer/{userId}")
    public ResponseEntity<DeveloperAnalyticsResponse> getDeveloperAnalytics(@PathVariable Long userId) {
        // TODO Implement developer analytics endpoint
        return ResponseEntity.ok(analyticsService.getDeveloperAnalytics(userId));
    }

    @GetMapping("/task-distribution")
    public ResponseEntity<TaskDistributionResponse> getTaskDistribution() {
        // TODO Implement task distribution endpoint
        return ResponseEntity.ok(analyticsService.getTaskDistribution());
    }

    @GetMapping("/velocity")
    public ResponseEntity<VelocityResponse> getVelocity() {
        // TODO Implement velocity endpoint
        return ResponseEntity.ok(analyticsService.getVelocity());
    }

    @GetMapping("/burndown")
    public ResponseEntity<BurndownResponse> getBurndown() {
        // TODO Implement burndown endpoint
        return ResponseEntity.ok(analyticsService.getBurndown());
    }

    @GetMapping("/issue-trend")
    public ResponseEntity<IssueTrendResponse> getIssueTrend() {
        return ResponseEntity.ok(analyticsService.getIssueTrend());
    }

    @GetMapping("/cycle-time")
    public ResponseEntity<CycleTimeResponse> getCycleTime() {
        return ResponseEntity.ok(analyticsService.getCycleTime());
    }

    @GetMapping("/deployment-frequency")
    public ResponseEntity<DeploymentFrequencyResponse> getDeploymentFrequency() {
        return ResponseEntity.ok(analyticsService.getDeploymentFrequency());
    }

    @GetMapping("/change-failure-rate")
    public ResponseEntity<ChangeFailureRateResponse> getChangeFailureRate() {
        return ResponseEntity.ok(analyticsService.getChangeFailureRate());
    }

    @GetMapping("/snapshots/{date}")
    public ResponseEntity<MetricsSnapshotResponse> getSnapshot(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(metricsSnapshotService.getSnapshot(date));
    }

    @GetMapping("/snapshots")
    public ResponseEntity<List<MetricsSnapshotResponse>> getSnapshots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(metricsSnapshotService.getSnapshots(startDate, endDate));
    }

    @GetMapping("/velocity-history")
    public ResponseEntity<VelocityHistoryResponse> getVelocityHistory() {
        return ResponseEntity.ok(velocityHistoryService.getVelocityHistory());
    }
}
