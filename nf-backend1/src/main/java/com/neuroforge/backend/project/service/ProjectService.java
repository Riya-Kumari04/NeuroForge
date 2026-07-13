package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.project.dto.CreateProjectRequest;
import com.neuroforge.backend.project.dto.ProjectDto;
import com.neuroforge.backend.project.dto.ProjectStatsDto;
import com.neuroforge.backend.project.dto.UpdateProjectRequest;

import java.util.List;

public interface ProjectService {

    ApiResponse<ProjectDto> createProject(CreateProjectRequest request);

    ApiResponse<List<ProjectDto>> getAllProjects();

    ApiResponse<List<ProjectDto>> getProjectsByOrganization(Long organizationId);

    ApiResponse<ProjectDto> getProjectById(Long id);

    ApiResponse<ProjectDto> updateProject(Long id, UpdateProjectRequest request);

    ApiResponse<Void> deleteProject(Long id);

    ApiResponse<ProjectStatsDto> getProjectStats(Long id);

}