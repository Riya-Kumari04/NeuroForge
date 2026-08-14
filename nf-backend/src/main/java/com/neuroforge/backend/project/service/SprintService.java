package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.project.dto.*;

import java.util.List;

public interface SprintService {

    ApiResponse<SprintDto> createSprint(CreateSprintRequest request);

    ApiResponse<List<SprintDto>> getAllSprints();

    ApiResponse<List<SprintDto>> getProjectSprints(Long projectId);

    ApiResponse<SprintDto> getSprintById(Long id);

    ApiResponse<SprintDto> updateSprint(Long id, UpdateSprintRequest request);

    ApiResponse<Void> deleteSprint(Long id);

    // Module 5: Sprint lifecycle methods
    ApiResponse<SprintDto> startSprint(Long id);
    ApiResponse<SprintDto> completeSprint(Long id);

    // Module 5: Sprint analytics methods
    ApiResponse<SprintSummaryResponse> getSprintSummary(Long id);
    ApiResponse<SprintStatisticsResponse> getSprintStatistics(Long id);
    ApiResponse<SprintProgressResponse> getSprintProgress(Long id);
    ApiResponse<List<BurndownPointResponse>> getSprintBurndown(Long id);
    ApiResponse<SprintVelocityResponse> getSprintVelocity(Long id);
    ApiResponse<TaskDistributionResponse> getTaskDistribution(Long id);
}