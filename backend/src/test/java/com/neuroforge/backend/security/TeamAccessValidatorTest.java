package com.neuroforge.backend.security;

import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.exception.AccessDeniedException;
import com.neuroforge.backend.service.CurrentUserContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TeamAccessValidatorTest {

    @Mock
    private CurrentUserContextService currentUserContextService;

    @InjectMocks
    private TeamAccessValidator teamAccessValidator;

    @Test
    void verifyTeamAccess_Success() {
        UUID teamId = UUID.randomUUID();
        Team team = Team.builder()
                .id(teamId)
                .build();

        when(currentUserContextService.getCurrentTeam()).thenReturn(team);

        assertDoesNotThrow(() -> teamAccessValidator.verifyTeamAccess(teamId));

        verify(currentUserContextService).getCurrentTeam();
    }

    @Test
    void verifyTeamAccess_Denied_ThrowsAccessDeniedException() {
        UUID teamId = UUID.randomUUID();
        UUID requestedId = UUID.randomUUID();
        Team team = Team.builder()
                .id(teamId)
                .build();

        when(currentUserContextService.getCurrentTeam()).thenReturn(team);

        assertThrows(AccessDeniedException.class, () -> teamAccessValidator.verifyTeamAccess(requestedId));

        verify(currentUserContextService).getCurrentTeam();
    }

    @Test
    void verifyTeamAccess_NullContext_ThrowsAccessDeniedException() {
        UUID requestedId = UUID.randomUUID();

        when(currentUserContextService.getCurrentTeam()).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> teamAccessValidator.verifyTeamAccess(requestedId));

        verify(currentUserContextService).getCurrentTeam();
    }
}
