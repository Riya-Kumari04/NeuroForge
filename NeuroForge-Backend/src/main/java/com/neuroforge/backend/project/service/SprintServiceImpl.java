package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.project.dto.CreateSprintRequest;
import com.neuroforge.backend.project.dto.SprintDto;
import com.neuroforge.backend.project.dto.UpdateSprintRequest;
import com.neuroforge.backend.project.entity.Project;
import com.neuroforge.backend.project.entity.Sprint;
import com.neuroforge.backend.project.repository.ProjectRepository;
import com.neuroforge.backend.project.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ApiResponse<SprintDto> createSprint(CreateSprintRequest request) {

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> AppException.notFound("Project not found"));

        Sprint sprint = Sprint.builder()
                .sprintName(request.getSprintName())
                .goal(request.getGoal())
                .status(request.getStatus() == null ? "PLANNED" : request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .project(project)
                .build();

        sprint = sprintRepository.save(sprint);

        return ApiResponse.ok("Sprint created successfully", SprintDto.from(sprint));
    }

    @Override
    public ApiResponse<List<SprintDto>> getAllSprints() {

        List<SprintDto> sprints = sprintRepository.findAll()
                .stream()
                .map(SprintDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok("Sprints retrieved successfully", sprints);
    }

    @Override
    public ApiResponse<List<SprintDto>> getProjectSprints(Long projectId) {

        List<SprintDto> sprints = sprintRepository.findByProjectId(projectId)
                .stream()
                .map(SprintDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok("Project sprints retrieved", sprints);
    }

    @Override
    public ApiResponse<SprintDto> getSprintById(Long id) {

        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        return ApiResponse.ok("Sprint found", SprintDto.from(sprint));
    }

    @Override
    @Transactional
    public ApiResponse<SprintDto> updateSprint(Long id, UpdateSprintRequest request) {

        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sprint not found"));

        if (request.getSprintName() != null)
            sprint.setSprintName(request.getSprintName());

        if (request.getGoal() != null)
            sprint.setGoal(request.getGoal());

        if (request.getStatus() != null)
            sprint.setStatus(request.getStatus());

        if (request.getStartDate() != null)
            sprint.setStartDate(request.getStartDate());

        if (request.getEndDate() != null)
            sprint.setEndDate(request.getEndDate());

        sprint = sprintRepository.save(sprint);

        return ApiResponse.ok("Sprint updated successfully", SprintDto.from(sprint));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteSprint(Long id) {

        if (!sprintRepository.existsById(id))
            throw AppException.notFound("Sprint not found");

        sprintRepository.deleteById(id);

        return ApiResponse.ok("Sprint deleted successfully");
    }
}