package com.neuroforge.backend.security;

import com.neuroforge.backend.entity.Organization;
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
public class OrganizationAccessValidatorTest {

    @Mock
    private CurrentUserContextService currentUserContextService;

    @InjectMocks
    private OrganizationAccessValidator organizationAccessValidator;

    @Test
    void verifyOrganizationAccess_Success() {
        UUID orgId = UUID.randomUUID();
        Organization org = Organization.builder()
                .id(orgId)
                .build();

        when(currentUserContextService.getCurrentOrganization()).thenReturn(org);

        assertDoesNotThrow(() -> organizationAccessValidator.verifyOrganizationAccess(orgId));

        verify(currentUserContextService).getCurrentOrganization();
    }

    @Test
    void verifyOrganizationAccess_Denied_ThrowsAccessDeniedException() {
        UUID orgId = UUID.randomUUID();
        UUID requestedId = UUID.randomUUID();
        Organization org = Organization.builder()
                .id(orgId)
                .build();

        when(currentUserContextService.getCurrentOrganization()).thenReturn(org);

        assertThrows(AccessDeniedException.class, () -> organizationAccessValidator.verifyOrganizationAccess(requestedId));

        verify(currentUserContextService).getCurrentOrganization();
    }

    @Test
    void verifyOrganizationAccess_NullContext_ThrowsAccessDeniedException() {
        UUID requestedId = UUID.randomUUID();

        when(currentUserContextService.getCurrentOrganization()).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> organizationAccessValidator.verifyOrganizationAccess(requestedId));

        verify(currentUserContextService).getCurrentOrganization();
    }
}
