package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CreateTeamMemberRequest;
import com.neuroforge.backend.dto.TeamMemberResponse;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.entity.TeamMember;
import com.neuroforge.backend.entity.TeamMemberRole;
import com.neuroforge.backend.entity.TeamMemberStatus;
import com.neuroforge.backend.exception.DuplicateResourceException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.TeamMemberRepository;
import com.neuroforge.backend.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public TeamMemberResponse createMember(UUID teamId, CreateTeamMemberRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + teamId));

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, request.getUserId())) {
            throw new DuplicateResourceException("User is already a member of this team: " + request.getUserId());
        }

        TeamMemberRole role = request.getRole() != null ? request.getRole() : TeamMemberRole.MEMBER;
        TeamMemberStatus status = request.getStatus() != null ? request.getStatus() : TeamMemberStatus.INVITED;

        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .userId(request.getUserId())
                .role(role)
                .status(status)
                .build();

        TeamMember saved = teamMemberRepository.save(teamMember);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getAllMembers(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }

        return teamMemberRepository.findByTeamId(teamId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamMemberResponse getMemberById(UUID teamId, UUID memberId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }

        TeamMember teamMember = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found with ID: " + memberId));

        if (!teamMember.getTeam().getId().equals(teamId)) {
            throw new ResourceNotFoundException("Team member does not belong to the specified team");
        }

        return mapToResponse(teamMember);
    }

    @Transactional
    public TeamMemberResponse updateMember(UUID teamId, UUID memberId, CreateTeamMemberRequest request) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }

        TeamMember teamMember = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found with ID: " + memberId));

        if (!teamMember.getTeam().getId().equals(teamId)) {
            throw new ResourceNotFoundException("Team member does not belong to the specified team");
        }

        if (teamMemberRepository.existsByTeamIdAndUserIdAndIdNot(teamId, request.getUserId(), memberId)) {
            throw new DuplicateResourceException("User is already a member of this team: " + request.getUserId());
        }

        teamMember.setUserId(request.getUserId());
        if (request.getRole() != null) {
            teamMember.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            teamMember.setStatus(request.getStatus());
        }

        TeamMember updated = teamMemberRepository.save(teamMember);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteMember(UUID teamId, UUID memberId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }

        TeamMember teamMember = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found with ID: " + memberId));

        if (!teamMember.getTeam().getId().equals(teamId)) {
            throw new ResourceNotFoundException("Team member does not belong to the specified team");
        }

        teamMemberRepository.delete(teamMember);
    }

    private TeamMemberResponse mapToResponse(TeamMember member) {
        return TeamMemberResponse.builder()
                .id(member.getId())
                .teamId(member.getTeam().getId())
                .userId(member.getUserId())
                .role(member.getRole())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .createdBy(member.getCreatedBy())
                .updatedAt(member.getUpdatedAt())
                .updatedBy(member.getUpdatedBy())
                .build();
    }
}
