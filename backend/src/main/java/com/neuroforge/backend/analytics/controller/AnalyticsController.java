package com.neuroforge.backend.analytics.controller;

import com.neuroforge.backend.analytics.dto.AnalyticsDashboardResponse;
import com.neuroforge.backend.analytics.dto.BurndownResponse;
import com.neuroforge.backend.analytics.dto.ChangeFailureRateResponse;
import com.neuroforge.backend.analytics.dto.CycleTimeResponse;
import com.neuroforge.backend.analytics.dto.DeveloperAnalyticsResponse;
import com.neuroforge.backend.analytics.dto.DeploymentFrequencyResponse;
import com.neuroforge.backend.analytics.dto.IssueTrendResponse;
import com.neuroforge.backend.analytics.dto.SprintAnalyticsResponse;
import com.neuroforge.backend.analytics.dto.TaskDistributionResponse;
import com.neuroforge.backend.analytics.dto.VelocityResponse;
import com.neuroforge.backend.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

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
}
