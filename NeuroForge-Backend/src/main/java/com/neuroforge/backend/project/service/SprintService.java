package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.project.dto.CreateSprintRequest;
import com.neuroforge.backend.project.dto.SprintDto;
import com.neuroforge.backend.project.dto.UpdateSprintRequest;

import java.util.List;

public interface SprintService {

    ApiResponse<SprintDto> createSprint(CreateSprintRequest request);

    ApiResponse<List<SprintDto>> getAllSprints();

    ApiResponse<List<SprintDto>> getProjectSprints(Long projectId);

    ApiResponse<SprintDto> getSprintById(Long id);

    ApiResponse<SprintDto> updateSprint(Long id, UpdateSprintRequest request);

    ApiResponse<Void> deleteSprint(Long id);

}