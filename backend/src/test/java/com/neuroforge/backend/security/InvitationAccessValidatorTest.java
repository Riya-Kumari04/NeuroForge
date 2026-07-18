package com.neuroforge.backend.security;

import com.neuroforge.backend.entity.Invitation;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.entity.TeamMember;
import com.neuroforge.backend.exception.AccessDeniedException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.InvitationRepository;
import com.neuroforge.backend.service.CurrentUserContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InvitationAccessValidatorTest {

    @Mock
    private CurrentUserContextService currentUserContextService;

    @Mock
    private InvitationRepository invitationRepository;

    @InjectMocks
    private InvitationAccessValidator invitationAccessValidator;

    private UUID teamId;
    private UUID invitationId;
    private Team team;
    private TeamMember teamMember;
    private Invitation invitation;

    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();
        invitationId = UUID.randomUUID();

        team = Team.builder()
                .id(teamId)
                .build();

        teamMember = TeamMember.builder()
                .team(team)
                .build();

        invitation = Invitation.builder()
                .id(invitationId)
                .team(team)
                .build();
    }

    @Test
    void verifyInvitationAccess_Success() {
        when(currentUserContextService.getCurrentTeamMember()).thenReturn(teamMember);
        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        assertDoesNotThrow(() -> invitationAccessValidator.verifyInvitationAccess(invitationId));

        verify(currentUserContextService).getCurrentTeamMember();
        verify(invitationRepository).findById(invitationId);
    }

    @Test
    void verifyInvitationAccess_Denied_ThrowsAccessDeniedException() {
        UUID otherTeamId = UUID.randomUUID();
        Team otherTeam = Team.builder().id(otherTeamId).build();
        Invitation otherInvitation = Invitation.builder().id(invitationId).team(otherTeam).build();

        when(currentUserContextService.getCurrentTeamMember()).thenReturn(teamMember);
        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(otherInvitation));

        assertThrows(AccessDeniedException.class, () -> invitationAccessValidator.verifyInvitationAccess(invitationId));

        verify(currentUserContextService).getCurrentTeamMember();
        verify(invitationRepository).findById(invitationId);
    }

    @Test
    void verifyInvitationAccess_NotFound_ThrowsResourceNotFoundException() {
        when(currentUserContextService.getCurrentTeamMember()).thenReturn(teamMember);
        when(invitationRepository.findById(invitationId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> invitationAccessValidator.verifyInvitationAccess(invitationId));

        verify(currentUserContextService).getCurrentTeamMember();
        verify(invitationRepository).findById(invitationId);
    }

    @Test
    void verifyInvitationAccess_NullContext_ThrowsAccessDeniedException() {
        when(currentUserContextService.getCurrentTeamMember()).thenReturn(null);
        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        assertThrows(AccessDeniedException.class, () -> invitationAccessValidator.verifyInvitationAccess(invitationId));

        verify(currentUserContextService).getCurrentTeamMember();
        verify(invitationRepository).findById(invitationId);
    }
}
