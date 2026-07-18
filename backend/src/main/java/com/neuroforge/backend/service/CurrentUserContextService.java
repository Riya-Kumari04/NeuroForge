package com.neuroforge.backend.service;

import com.neuroforge.backend.entity.Organization;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.entity.TeamMember;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentUserContextService {

    private final TeamMemberRepository teamMemberRepository;

    /**
     * Read the authenticated email from SecurityContextHolder.
     *
     * @return the authenticated email, or null if not authenticated
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Find TeamMember by email.
     * Throw ResourceNotFoundException if not found.
     *
     * @return the current authenticated TeamMember
     */
    public TeamMember getCurrentTeamMember() {
        String email = getCurrentUserEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new ResourceNotFoundException("No authenticated user found in security context");
        }
        return teamMemberRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember not found for email: " + email));
    }

    /**
     * Return getCurrentTeamMember().getTeam()
     *
     * @return the current authenticated Team
     */
    public Team getCurrentTeam() {
        TeamMember teamMember = getCurrentTeamMember();
        Team team = teamMember.getTeam();
        if (team == null) {
            throw new ResourceNotFoundException("Team not found for current member");
        }
        return team;
    }

    /**
     * Return getCurrentTeam().getOrganization()
     *
     * @return the current authenticated Organization
     */
    public Organization getCurrentOrganization() {
        Team team = getCurrentTeam();
        Organization organization = team.getOrganization();
        if (organization == null) {
            throw new ResourceNotFoundException("Organization not found for current team");
        }
        return organization;
    }
}
