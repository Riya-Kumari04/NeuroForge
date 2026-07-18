package com.neuroforge.backend.security;

import com.neuroforge.backend.entity.Organization;
import com.neuroforge.backend.exception.AccessDeniedException;
import com.neuroforge.backend.service.CurrentUserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationAccessValidator {

    private final CurrentUserContextService currentUserContextService;

    /**
     * Verify access to the requested organization.
     *
     * @param organizationId the requested organization ID
     * @throws AccessDeniedException if access is denied
     */
    public void verifyOrganizationAccess(UUID organizationId) {
        Organization currentOrg = currentUserContextService.getCurrentOrganization();
        if (currentOrg == null || !currentOrg.getId().equals(organizationId)) {
            throw new AccessDeniedException("Access denied to organization ID: " + organizationId);
        }
    }
}
