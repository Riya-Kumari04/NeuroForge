package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CreateSprintRequest;
import com.neuroforge.backend.dto.SprintResponse;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintRepository sprintRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public SprintResponse createSprint(CreateSprintRequest request) {
        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + request.getTeamId()));
        }

        SprintStatus status = request.getStatus() != null ? request.getStatus() : SprintStatus.PLANNED;

        Sprint sprint = Sprint.builder()
                .name(request.getName())
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(status)
                .team(team)
                .build();

        Sprint saved = sprintRepository.save(sprint);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> getAllSprints() {
        return sprintRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SprintResponse getSprintById(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + id));
        return mapToResponse(sprint);
    }

    @Transactional
    public SprintResponse updateSprint(UUID id, CreateSprintRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + id));

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + request.getTeamId()));
            sprint.setTeam(team);
        } else {
            sprint.setTeam(null);
        }

        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            sprint.setStatus(request.getStatus());
        }

        Sprint updated = sprintRepository.save(sprint);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteSprint(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + id));
        sprintRepository.delete(sprint);
    }

    private SprintResponse mapToResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .status(sprint.getStatus())
                .teamId(sprint.getTeam() != null ? sprint.getTeam().getId() : null)
                .createdAt(sprint.getCreatedAt())
                .createdBy(sprint.getCreatedBy())
                .updatedAt(sprint.getUpdatedAt())
                .updatedBy(sprint.getUpdatedBy())
                .build();
    }
}
