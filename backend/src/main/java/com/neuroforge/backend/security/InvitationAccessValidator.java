package com.neuroforge.backend.security;

import com.neuroforge.backend.entity.Invitation;
import com.neuroforge.backend.entity.TeamMember;
import com.neuroforge.backend.exception.AccessDeniedException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.InvitationRepository;
import com.neuroforge.backend.service.CurrentUserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationAccessValidator {

    private final CurrentUserContextService currentUserContextService;
    private final InvitationRepository invitationRepository;

    /**
     * Verify access to the requested invitation.
     *
     * @param invitationId the requested invitation ID
     * @throws AccessDeniedException if access is denied
     * @throws ResourceNotFoundException if the invitation does not exist
     */
    public void verifyInvitationAccess(UUID invitationId) {
        TeamMember currentMember = currentUserContextService.getCurrentTeamMember();
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with ID: " + invitationId));

        if (currentMember == null || !invitation.getTeam().getId().equals(currentMember.getTeam().getId())) {
            throw new AccessDeniedException("Access denied to invitation ID: " + invitationId);
        }
    }
}
