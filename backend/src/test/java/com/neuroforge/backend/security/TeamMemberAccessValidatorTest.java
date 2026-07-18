package com.neuroforge.backend.security;

import com.neuroforge.backend.entity.TeamMember;
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
public class TeamMemberAccessValidatorTest {

    @Mock
    private CurrentUserContextService currentUserContextService;

    @InjectMocks
    private TeamMemberAccessValidator teamMemberAccessValidator;

    @Test
    void verifyTeamMemberAccess_Success() {
        UUID memberId = UUID.randomUUID();
        TeamMember member = TeamMember.builder()
                .id(memberId)
                .build();

        when(currentUserContextService.getCurrentTeamMember()).thenReturn(member);

        assertDoesNotThrow(() -> teamMemberAccessValidator.verifyTeamMemberAccess(memberId));

        verify(currentUserContextService).getCurrentTeamMember();
    }

    @Test
    void verifyTeamMemberAccess_Denied_ThrowsAccessDeniedException() {
        UUID memberId = UUID.randomUUID();
        UUID requestedId = UUID.randomUUID();
        TeamMember member = TeamMember.builder()
                .id(memberId)
                .build();

        when(currentUserContextService.getCurrentTeamMember()).thenReturn(member);

        assertThrows(AccessDeniedException.class, () -> teamMemberAccessValidator.verifyTeamMemberAccess(requestedId));

        verify(currentUserContextService).getCurrentTeamMember();
    }

    @Test
    void verifyTeamMemberAccess_NullContext_ThrowsAccessDeniedException() {
        UUID requestedId = UUID.randomUUID();

        when(currentUserContextService.getCurrentTeamMember()).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> teamMemberAccessValidator.verifyTeamMemberAccess(requestedId));

        verify(currentUserContextService).getCurrentTeamMember();
    }
}
