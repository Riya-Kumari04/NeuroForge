package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CreateTeamRequest;
import com.neuroforge.backend.dto.TeamResponse;
import com.neuroforge.backend.entity.Organization;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.entity.TeamStatus;
import com.neuroforge.backend.exception.DuplicateResourceException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.OrganizationRepository;
import com.neuroforge.backend.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public TeamResponse createTeam(UUID organizationId, CreateTeamRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + organizationId));

        if (teamRepository.existsByOrganizationIdAndName(organizationId, request.getName())) {
            throw new DuplicateResourceException("Team name already exists in this organization: " + request.getName());
        }

        TeamStatus status = request.getStatus() != null ? request.getStatus() : TeamStatus.ACTIVE;

        Team team = Team.builder()
                .organization(organization)
                .name(request.getName())
                .description(request.getDescription())
                .status(status)
                .build();

        Team saved = teamRepository.save(team);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams(UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found with ID: " + organizationId);
        }

        return teamRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeamById(UUID organizationId, UUID teamId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found with ID: " + organizationId);
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + teamId));

        if (!team.getOrganization().getId().equals(organizationId)) {
            throw new ResourceNotFoundException("Team does not belong to the specified organization");
        }

        return mapToResponse(team);
    }

    @Transactional
    public TeamResponse updateTeam(UUID organizationId, UUID teamId, CreateTeamRequest request) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found with ID: " + organizationId);
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + teamId));

        if (!team.getOrganization().getId().equals(organizationId)) {
            throw new ResourceNotFoundException("Team does not belong to the specified organization");
        }

        if (teamRepository.existsByOrganizationIdAndNameAndIdNot(organizationId, request.getName(), teamId)) {
            throw new DuplicateResourceException("Team name already exists in this organization: " + request.getName());
        }

        team.setName(request.getName());
        team.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            team.setStatus(request.getStatus());
        }

        Team updated = teamRepository.save(team);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteTeam(UUID organizationId, UUID teamId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found with ID: " + organizationId);
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + teamId));

        if (!team.getOrganization().getId().equals(organizationId)) {
            throw new ResourceNotFoundException("Team does not belong to the specified organization");
        }

        teamRepository.delete(team);
    }

    private TeamResponse mapToResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .organizationId(team.getOrganization().getId())
                .name(team.getName())
                .description(team.getDescription())
                .status(team.getStatus())
                .createdAt(team.getCreatedAt())
                .createdBy(team.getCreatedBy())
                .updatedAt(team.getUpdatedAt())
                .updatedBy(team.getUpdatedBy())
                .build();
    }
}
