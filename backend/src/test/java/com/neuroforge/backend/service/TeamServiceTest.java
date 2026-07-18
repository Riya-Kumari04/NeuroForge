package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CreateTeamRequest;
import com.neuroforge.backend.dto.TeamResponse;
import com.neuroforge.backend.entity.Organization;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.entity.TeamStatus;
import com.neuroforge.backend.exception.DuplicateResourceException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.exception.AccessDeniedException;
import com.neuroforge.backend.repository.OrganizationRepository;
import com.neuroforge.backend.repository.TeamRepository;
import com.neuroforge.backend.security.TeamAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private TeamAccessValidator teamAccessValidator;

    @InjectMocks
    private TeamService teamService;

    private UUID orgId;
    private UUID teamId;
    private Organization organization;
    private CreateTeamRequest request;
    private Team team;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        teamId = UUID.randomUUID();

        organization = Organization.builder()
                .id(orgId)
                .name("Neuro Forge Org")
                .slug("neuro-forge-org")
                .build();

        request = CreateTeamRequest.builder()
                .name("Backend Team")
                .description("Handles backend APIs")
                .status(TeamStatus.ACTIVE)
                .build();

        team = Team.builder()
                .id(teamId)
                .organization(organization)
                .name("Backend Team")
                .description("Handles backend APIs")
                .status(TeamStatus.ACTIVE)
                .build();
    }

    @Test
    void createTeam_Success() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(teamRepository.existsByOrganizationIdAndName(orgId, request.getName())).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        TeamResponse response = teamService.createTeam(orgId, request);

        assertNotNull(response);
        assertEquals(teamId, response.getId());
        assertEquals(orgId, response.getOrganizationId());
        assertEquals(request.getName(), response.getName());

        verify(organizationRepository).findById(orgId);
        verify(teamRepository).existsByOrganizationIdAndName(orgId, request.getName());
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void createTeam_OrgNotFound_ThrowsException() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.createTeam(orgId, request));

        verify(organizationRepository).findById(orgId);
        verify(teamRepository, never()).existsByOrganizationIdAndName(any(UUID.class), anyString());
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void createTeam_DuplicateName_ThrowsException() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(teamRepository.existsByOrganizationIdAndName(orgId, request.getName())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> teamService.createTeam(orgId, request));

        verify(organizationRepository).findById(orgId);
        verify(teamRepository).existsByOrganizationIdAndName(orgId, request.getName());
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void getAllTeams_Success() {
        when(organizationRepository.existsById(orgId)).thenReturn(true);
        when(teamRepository.findByOrganizationId(orgId)).thenReturn(Arrays.asList(team));

        List<TeamResponse> list = teamService.getAllTeams(orgId);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(teamId, list.get(0).getId());

        verify(organizationRepository).existsById(orgId);
        verify(teamRepository).findByOrganizationId(orgId);
    }

    @Test
    void getAllTeams_OrgNotFound_ThrowsException() {
        when(organizationRepository.existsById(orgId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> teamService.getAllTeams(orgId));

        verify(organizationRepository).existsById(orgId);
        verify(teamRepository, never()).findByOrganizationId(any(UUID.class));
    }

    @Test
    void getTeamById_Success() {
        doNothing().when(teamAccessValidator).verifyTeamAccess(teamId);
        when(organizationRepository.existsById(orgId)).thenReturn(true);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        TeamResponse response = teamService.getTeamById(orgId, teamId);

        assertNotNull(response);
        assertEquals(teamId, response.getId());

        verify(teamAccessValidator).verifyTeamAccess(teamId);
        verify(organizationRepository).existsById(orgId);
        verify(teamRepository).findById(teamId);
    }

    @Test
    void getTeamById_AccessDenied_ThrowsAccessDeniedException() {
        doThrow(new AccessDeniedException("Access denied")).when(teamAccessValidator).verifyTeamAccess(teamId);

        assertThrows(AccessDeniedException.class, () -> teamService.getTeamById(orgId, teamId));

        verify(teamAccessValidator).verifyTeamAccess(teamId);
        verify(organizationRepository, never()).existsById(any(UUID.class));
        verify(teamRepository, never()).findById(any(UUID.class));
    }

    @Test
    void getTeamById_NotBelonging_ThrowsException() {
        UUID otherOrgId = UUID.randomUUID();
        doNothing().when(teamAccessValidator).verifyTeamAccess(teamId);
        when(organizationRepository.existsById(otherOrgId)).thenReturn(true);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team)); // belongs to orgId

        assertThrows(ResourceNotFoundException.class, () -> teamService.getTeamById(otherOrgId, teamId));

        verify(teamAccessValidator).verifyTeamAccess(teamId);
        verify(organizationRepository).existsById(otherOrgId);
        verify(teamRepository).findById(teamId);
    }

    @Test
    void updateTeam_Success() {
        CreateTeamRequest updateReq = CreateTeamRequest.builder()
                .name("Frontend Team")
                .description("Handles frontend react apps")
                .status(TeamStatus.INACTIVE)
                .build();

        Team updatedTeam = Team.builder()
                .id(teamId)
                .organization(organization)
                .name("Frontend Team")
                .description("Handles frontend react apps")
                .status(TeamStatus.INACTIVE)
                .build();

        doNothing().when(teamAccessValidator).verifyTeamAccess(teamId);
        when(organizationRepository.existsById(orgId)).thenReturn(true);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamRepository.existsByOrganizationIdAndNameAndIdNot(orgId, updateReq.getName(), teamId)).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(updatedTeam);

        TeamResponse response = teamService.updateTeam(orgId, teamId, updateReq);

        assertNotNull(response);
        assertEquals(updateReq.getName(), response.getName());
        assertEquals(TeamStatus.INACTIVE, response.getStatus());

        verify(teamAccessValidator).verifyTeamAccess(teamId);
        verify(organizationRepository).existsById(orgId);
        verify(teamRepository).findById(teamId);
        verify(teamRepository).existsByOrganizationIdAndNameAndIdNot(orgId, updateReq.getName(), teamId);
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void updateTeam_AccessDenied_ThrowsAccessDeniedException() {
        CreateTeamRequest updateReq = CreateTeamRequest.builder().name("Frontend").build();
        doThrow(new AccessDeniedException("Access denied")).when(teamAccessValidator).verifyTeamAccess(teamId);

        assertThrows(AccessDeniedException.class, () -> teamService.updateTeam(orgId, teamId, updateReq));

        verify(teamAccessValidator).verifyTeamAccess(teamId);
        verify(organizationRepository, never()).existsById(any(UUID.class));
        verify(teamRepository, never()).findById(any(UUID.class));
    }

    @Test
    void deleteTeam_Success() {
        doNothing().when(teamAccessValidator).verifyTeamAccess(teamId);
        when(organizationRepository.existsById(orgId)).thenReturn(true);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        doNothing().when(teamRepository).delete(team);

        teamService.deleteTeam(orgId, teamId);

        verify(teamAccessValidator).verifyTeamAccess(teamId);
        verify(organizationRepository).existsById(orgId);
        verify(teamRepository).findById(teamId);
        verify(teamRepository).delete(team);
    }

    @Test
    void deleteTeam_AccessDenied_ThrowsAccessDeniedException() {
        doThrow(new AccessDeniedException("Access denied")).when(teamAccessValidator).verifyTeamAccess(teamId);

        assertThrows(AccessDeniedException.class, () -> teamService.deleteTeam(orgId, teamId));

        verify(teamAccessValidator).verifyTeamAccess(teamId);
        verify(organizationRepository, never()).existsById(any(UUID.class));
        verify(teamRepository, never()).findById(any(UUID.class));
    }
}
