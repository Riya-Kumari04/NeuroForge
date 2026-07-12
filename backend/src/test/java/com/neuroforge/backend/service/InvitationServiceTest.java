package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CreateInvitationRequest;
import com.neuroforge.backend.dto.InvitationResponse;
import com.neuroforge.backend.entity.Invitation;
import com.neuroforge.backend.entity.InvitationStatus;
import com.neuroforge.backend.entity.Organization;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.exception.DuplicateResourceException;
import com.neuroforge.backend.exception.InvalidInvitationStateException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.InvitationRepository;
import com.neuroforge.backend.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private InvitationService invitationService;

    private UUID orgId;
    private UUID teamId;
    private UUID inviteId;
    private UUID inviterId;
    private Organization organization;
    private Team team;
    private CreateInvitationRequest request;
    private Invitation invitation;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        inviteId = UUID.randomUUID();
        inviterId = UUID.randomUUID();

        organization = Organization.builder()
                .id(orgId)
                .name("Neuro Forge")
                .slug("neuro-forge")
                .build();

        team = Team.builder()
                .id(teamId)
                .organization(organization)
                .name("Core Team")
                .build();

        request = CreateInvitationRequest.builder()
                .email("invitee@example.com")
                .invitedBy(inviterId)
                .build();

        invitation = Invitation.builder()
                .id(inviteId)
                .organization(organization)
                .team(team)
                .email("invitee@example.com")
                .invitationToken("test-token")
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .invitedBy(inviterId)
                .build();
    }

    @Test
    void createInvitation_Success() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(invitationRepository.existsByTeamIdAndEmailAndStatus(teamId, request.getEmail(), InvitationStatus.PENDING)).thenReturn(false);
        when(invitationRepository.save(any(Invitation.class))).thenReturn(invitation);

        InvitationResponse response = invitationService.createInvitation(teamId, request);

        assertNotNull(response);
        assertEquals(inviteId, response.getId());
        assertEquals(orgId, response.getOrganizationId());
        assertEquals(teamId, response.getTeamId());
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(InvitationStatus.PENDING, response.getStatus());

        verify(teamRepository).findById(teamId);
        verify(invitationRepository).existsByTeamIdAndEmailAndStatus(teamId, request.getEmail(), InvitationStatus.PENDING);
        verify(invitationRepository).save(any(Invitation.class));
    }

    @Test
    void createInvitation_TeamNotFound_ThrowsException() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> invitationService.createInvitation(teamId, request));

        verify(teamRepository).findById(teamId);
        verify(invitationRepository, never()).existsByTeamIdAndEmailAndStatus(any(), any(), any());
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void createInvitation_DuplicatePending_ThrowsException() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(invitationRepository.existsByTeamIdAndEmailAndStatus(teamId, request.getEmail(), InvitationStatus.PENDING)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> invitationService.createInvitation(teamId, request));

        verify(teamRepository).findById(teamId);
        verify(invitationRepository).existsByTeamIdAndEmailAndStatus(teamId, request.getEmail(), InvitationStatus.PENDING);
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void getAllInvitations_Success() {
        when(teamRepository.existsById(teamId)).thenReturn(true);
        when(invitationRepository.findByTeamId(teamId)).thenReturn(Arrays.asList(invitation));

        List<InvitationResponse> list = invitationService.getAllInvitations(teamId);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(inviteId, list.get(0).getId());

        verify(teamRepository).existsById(teamId);
        verify(invitationRepository).findByTeamId(teamId);
    }

    @Test
    void getInvitationById_Success() {
        when(teamRepository.existsById(teamId)).thenReturn(true);
        when(invitationRepository.findById(inviteId)).thenReturn(Optional.of(invitation));

        InvitationResponse response = invitationService.getInvitationById(teamId, inviteId);

        assertNotNull(response);
        assertEquals(inviteId, response.getId());

        verify(teamRepository).existsById(teamId);
        verify(invitationRepository).findById(inviteId);
    }

    @Test
    void deleteInvitation_Success() {
        when(teamRepository.existsById(teamId)).thenReturn(true);
        when(invitationRepository.findById(inviteId)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invitationService.deleteInvitation(teamId, inviteId);

        assertEquals(InvitationStatus.CANCELLED, invitation.getStatus());
        verify(teamRepository).existsById(teamId);
        verify(invitationRepository).findById(inviteId);
        verify(invitationRepository).save(invitation);
    }

    @Test
    void acceptInvitation_Success() {
        when(invitationRepository.findByInvitationToken("test-token")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

        InvitationResponse response = invitationService.acceptInvitation("test-token");

        assertNotNull(response);
        assertEquals(InvitationStatus.ACCEPTED, response.getStatus());

        verify(invitationRepository).findByInvitationToken("test-token");
        verify(invitationRepository).save(invitation);
    }

    @Test
    void acceptInvitation_Expired_ThrowsException() {
        invitation.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(invitationRepository.findByInvitationToken("test-token")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(InvalidInvitationStateException.class, () -> invitationService.acceptInvitation("test-token"));
        assertEquals(InvitationStatus.EXPIRED, invitation.getStatus());

        verify(invitationRepository).findByInvitationToken("test-token");
        verify(invitationRepository).save(invitation);
    }

    @Test
    void acceptInvitation_NotPending_ThrowsException() {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findByInvitationToken("test-token")).thenReturn(Optional.of(invitation));

        assertThrows(InvalidInvitationStateException.class, () -> invitationService.acceptInvitation("test-token"));

        verify(invitationRepository).findByInvitationToken("test-token");
        verify(invitationRepository, never()).save(any());
    }
}
