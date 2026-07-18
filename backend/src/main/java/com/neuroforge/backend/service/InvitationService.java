package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CreateInvitationRequest;
import com.neuroforge.backend.dto.InvitationResponse;
import com.neuroforge.backend.entity.Invitation;
import com.neuroforge.backend.entity.InvitationStatus;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.exception.DuplicateResourceException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.exception.InvalidInvitationStateException;
import com.neuroforge.backend.repository.InvitationRepository;
import com.neuroforge.backend.repository.TeamRepository;
import com.neuroforge.backend.security.InvitationAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final TeamRepository teamRepository;
    private final InvitationAccessValidator invitationAccessValidator;

    @Transactional
    public InvitationResponse createInvitation(UUID teamId, CreateInvitationRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + teamId));

        if (invitationRepository.existsByTeamIdAndEmailAndStatus(teamId, request.getEmail(),
                InvitationStatus.PENDING)) {
            throw new DuplicateResourceException(
                    "A pending invitation for this email already exists in this team: " + request.getEmail());
        }

        Invitation invitation = Invitation.builder()
                .organization(team.getOrganization())
                .team(team)
                .email(request.getEmail())
                .invitationToken(UUID.randomUUID().toString())
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .invitedBy(request.getInvitedBy())
                .build();

        Invitation saved = invitationRepository.save(invitation);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getAllInvitations(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }

        return invitationRepository.findByTeamId(teamId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvitationResponse getInvitationById(UUID teamId, UUID id) {
        invitationAccessValidator.verifyInvitationAccess(id);

        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }

        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with ID: " + id));

        if (!invitation.getTeam().getId().equals(teamId)) {
            throw new ResourceNotFoundException("Invitation does not belong to the specified team");
        }

        return mapToResponse(invitation);
    }

    @Transactional
    public void deleteInvitation(UUID teamId, UUID id) {
        invitationAccessValidator.verifyInvitationAccess(id);

        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }

        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with ID: " + id));

        if (!invitation.getTeam().getId().equals(teamId)) {
            throw new ResourceNotFoundException("Invitation does not belong to the specified team");
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        invitationRepository.save(invitation);
    }

    @Transactional
    public InvitationResponse acceptInvitation(String token) {
        Invitation invitation = invitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found for token: " + token));

        if (invitation.getStatus() == InvitationStatus.EXPIRED
                || LocalDateTime.now().isAfter(invitation.getExpiresAt())) {
            if (invitation.getStatus() == InvitationStatus.PENDING) {
                invitation.setStatus(InvitationStatus.EXPIRED);
                invitationRepository.save(invitation);
            }
            throw new InvalidInvitationStateException("Invitation has expired");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidInvitationStateException(
                    "Invitation is not pending (status: " + invitation.getStatus() + ")");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        Invitation saved = invitationRepository.save(invitation);

        // TODO Future integration:
        // After Module 1 authentication service integration,
        // automatically create TeamMember from accepted invitation.

        return mapToResponse(saved);
    }

    private InvitationResponse mapToResponse(Invitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .organizationId(invitation.getOrganization().getId())
                .teamId(invitation.getTeam().getId())
                .email(invitation.getEmail())
                .invitationToken(invitation.getInvitationToken())
                .status(invitation.getStatus())
                .expiresAt(invitation.getExpiresAt())
                .invitedBy(invitation.getInvitedBy())
                .createdAt(invitation.getCreatedAt())
                .createdBy(invitation.getCreatedBy())
                .updatedAt(invitation.getUpdatedAt())
                .updatedBy(invitation.getUpdatedBy())
                .build();
    }
}
