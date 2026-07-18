package com.neuroforge.backend.security;

import com.neuroforge.backend.entity.TeamMember;
import com.neuroforge.backend.exception.AccessDeniedException;
import com.neuroforge.backend.service.CurrentUserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamMemberAccessValidator {

    private final CurrentUserContextService currentUserContextService;

    /**
     * Verify access to the requested team member.
     *
     * @param memberId the requested team member ID
     * @throws AccessDeniedException if access is denied
     */
    public void verifyTeamMemberAccess(UUID memberId) {
        TeamMember currentMember = currentUserContextService.getCurrentTeamMember();
        if (currentMember == null || !currentMember.getId().equals(memberId)) {
            throw new AccessDeniedException("Access denied to team member ID: " + memberId);
        }
    }
}
