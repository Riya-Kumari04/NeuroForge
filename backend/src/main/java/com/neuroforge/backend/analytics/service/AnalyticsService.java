package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.AnalyticsDashboardResponse;
import com.neuroforge.backend.analytics.dto.BurndownResponse;
import com.neuroforge.backend.analytics.dto.DeveloperAnalyticsResponse;
import com.neuroforge.backend.analytics.dto.SprintAnalyticsResponse;
import com.neuroforge.backend.analytics.dto.TaskDistributionResponse;
import com.neuroforge.backend.analytics.dto.VelocityResponse;

import java.util.UUID;

public interface AnalyticsService {

    AnalyticsDashboardResponse getDashboard();

    SprintAnalyticsResponse getSprintAnalytics(UUID sprintId);

    DeveloperAnalyticsResponse getDeveloperAnalytics(Long userId);

    TaskDistributionResponse getTaskDistribution();

    VelocityResponse getVelocity();

    BurndownResponse getBurndown();
}
