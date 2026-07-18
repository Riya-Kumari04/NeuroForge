package com.neuroforge.backend.security;

import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.exception.AccessDeniedException;
import com.neuroforge.backend.service.CurrentUserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamAccessValidator {

    private final CurrentUserContextService currentUserContextService;

    /**
     * Verify access to the requested team.
     *
     * @param teamId the requested team ID
     * @throws AccessDeniedException if access is denied
     */
    public void verifyTeamAccess(UUID teamId) {
        Team currentTeam = currentUserContextService.getCurrentTeam();
        if (currentTeam == null || !currentTeam.getId().equals(teamId)) {
            throw new AccessDeniedException("Access denied to team ID: " + teamId);
        }
    }
}
