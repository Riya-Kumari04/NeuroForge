package com.neuroforge.backend.service;

import com.neuroforge.backend.entity.Organization;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.entity.TeamMember;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.TeamMemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CurrentUserContextServiceTest {

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private CurrentUserContextService currentUserContextService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserEmail_Authenticated_ReturnsEmail() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");

        String email = currentUserContextService.getCurrentUserEmail();

        assertEquals("user@example.com", email);
    }

    @Test
    void getCurrentUserEmail_Unauthenticated_ReturnsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        String email = currentUserContextService.getCurrentUserEmail();

        assertNull(email);
    }

    @Test
    void getCurrentTeamMember_Success_ReturnsTeamMember() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");

        TeamMember teamMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .build();
        when(teamMemberRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(teamMember));

        TeamMember result = currentUserContextService.getCurrentTeamMember();

        assertNotNull(result);
        assertEquals(teamMember.getId(), result.getId());
    }

    @Test
    void getCurrentTeamMember_NoEmail_ThrowsResourceNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> currentUserContextService.getCurrentTeamMember());
    }

    @Test
    void getCurrentTeamMember_NotFoundInRepo_ThrowsResourceNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");
        when(teamMemberRepository.findByUserEmail("user@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> currentUserContextService.getCurrentTeamMember());
    }

    @Test
    void getCurrentTeam_Success_ReturnsTeam() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");

        Team team = Team.builder()
                .id(UUID.randomUUID())
                .name("Development Team")
                .build();
        TeamMember teamMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .team(team)
                .build();
        when(teamMemberRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(teamMember));

        Team result = currentUserContextService.getCurrentTeam();

        assertNotNull(result);
        assertEquals(team.getId(), result.getId());
        assertEquals("Development Team", result.getName());
    }

    @Test
    void getCurrentTeam_NoTeam_ThrowsResourceNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");

        TeamMember teamMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .team(null)
                .build();
        when(teamMemberRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(teamMember));

        assertThrows(ResourceNotFoundException.class, () -> currentUserContextService.getCurrentTeam());
    }

    @Test
    void getCurrentOrganization_Success_ReturnsOrganization() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");

        Organization org = Organization.builder()
                .id(UUID.randomUUID())
                .name("NeuroForge")
                .build();
        Team team = Team.builder()
                .id(UUID.randomUUID())
                .organization(org)
                .build();
        TeamMember teamMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .team(team)
                .build();
        when(teamMemberRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(teamMember));

        Organization result = currentUserContextService.getCurrentOrganization();

        assertNotNull(result);
        assertEquals(org.getId(), result.getId());
        assertEquals("NeuroForge", result.getName());
    }

    @Test
    void getCurrentOrganization_NoOrganization_ThrowsResourceNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");

        Team team = Team.builder()
                .id(UUID.randomUUID())
                .organization(null)
                .build();
        TeamMember teamMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .team(team)
                .build();
        when(teamMemberRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(teamMember));

        assertThrows(ResourceNotFoundException.class, () -> currentUserContextService.getCurrentOrganization());
    }
}
