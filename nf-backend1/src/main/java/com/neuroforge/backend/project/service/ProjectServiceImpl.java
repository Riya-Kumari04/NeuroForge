package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.organization.entity.Organization;
import com.neuroforge.backend.organization.repository.OrganizationRepository;
import com.neuroforge.backend.project.dto.CreateProjectRequest;
import com.neuroforge.backend.project.dto.ProjectDto;
import com.neuroforge.backend.project.dto.ProjectStatsDto;
import com.neuroforge.backend.project.dto.UpdateProjectRequest;
import com.neuroforge.backend.project.entity.Project;
import com.neuroforge.backend.project.repository.ProjectMemberRepository;
import com.neuroforge.backend.project.repository.ProjectRepository;
import com.neuroforge.backend.project.repository.SprintRepository;
import com.neuroforge.backend.project.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public ApiResponse<ProjectDto> createProject(CreateProjectRequest request) {

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> AppException.notFound("Organization not found"));

        Project project = Project.builder()
                .projectName(request.getProjectName())
                .description(request.getDescription())
                .status(request.getStatus() == null ? "ACTIVE" : request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .organization(organization)
                .build();

        project = projectRepository.save(project);

        return ApiResponse.ok("Project created successfully", ProjectDto.from(project));
    }

    @Override
    public ApiResponse<List<ProjectDto>> getAllProjects() {

        List<ProjectDto> projects = projectRepository.findAll()
                .stream()
                .map(ProjectDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok("Projects retrieved successfully", projects);
    }

    @Override
    public ApiResponse<List<ProjectDto>> getProjectsByOrganization(Long organizationId) {
        List<ProjectDto> projects = projectRepository.findByOrganizationId(organizationId)
                .stream()
                .map(ProjectDto::from)
                .collect(Collectors.toList());
        return ApiResponse.ok("Projects retrieved successfully", projects);
    }

    @Override
    public ApiResponse<ProjectDto> getProjectById(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Project not found"));

        return ApiResponse.ok("Project found", ProjectDto.from(project));
    }

    @Override
    @Transactional
    public ApiResponse<ProjectDto> updateProject(Long id, UpdateProjectRequest request) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Project not found"));

        if (request.getProjectName() != null)
            project.setProjectName(request.getProjectName());

        if (request.getDescription() != null)
            project.setDescription(request.getDescription());

        if (request.getStatus() != null)
            project.setStatus(request.getStatus());

        if (request.getStartDate() != null)
            project.setStartDate(request.getStartDate());

        if (request.getEndDate() != null)
            project.setEndDate(request.getEndDate());

        project = projectRepository.save(project);

        return ApiResponse.ok("Project updated successfully", ProjectDto.from(project));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteProject(Long id) {

        if (!projectRepository.existsById(id))
            throw AppException.notFound("Project not found");

        projectRepository.deleteById(id);

        return ApiResponse.ok("Project deleted successfully");
    }

    @Override
    public ApiResponse<ProjectStatsDto> getProjectStats(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Project not found"));

        long totalTasks = taskRepository.countByProjectId(id);
        long completedTasks = taskRepository.countByProjectIdAndStatus(id, "DONE");
        long inProgressTasks = taskRepository.countByProjectIdAndStatus(id, "IN_PROGRESS");
        long todoTasks = taskRepository.countByProjectIdAndStatus(id, "TODO");
        long totalSprints = sprintRepository.findByProjectId(id).size();
        long totalMembers = projectMemberRepository.findByProject(project).size();

        int healthScore = 100;
        if (totalTasks > 0) {
            double completionRate = (double) completedTasks / totalTasks;
            healthScore = (int) (completionRate * 100);
        }
        if ("COMPLETED".equals(project.getStatus())) healthScore = 100;

        String healthStatus;
        if (healthScore >= 70) healthStatus = "HEALTHY";
        else if (healthScore >= 40) healthStatus = "AT_RISK";
        else healthStatus = "CRITICAL";

        ProjectStatsDto stats = ProjectStatsDto.builder()
                .projectId(id)
                .projectName(project.getProjectName())
                .status(project.getStatus())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .todoTasks(todoTasks)
                .totalSprints(totalSprints)
                .totalMembers(totalMembers)
                .healthScore(healthScore)
                .healthStatus(healthStatus)
                .build();

        return ApiResponse.ok("Project stats retrieved", stats);
    }
}