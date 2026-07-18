package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CreateOrganizationRequest;
import com.neuroforge.backend.dto.OrganizationResponse;
import com.neuroforge.backend.entity.Organization;
import com.neuroforge.backend.entity.OrganizationStatus;
import com.neuroforge.backend.exception.DuplicateResourceException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.exception.AccessDeniedException;
import com.neuroforge.backend.repository.OrganizationRepository;
import com.neuroforge.backend.security.OrganizationAccessValidator;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationAccessValidator organizationAccessValidator;

    @InjectMocks
    private OrganizationService organizationService;

    private CreateOrganizationRequest request;
    private Organization organization;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        request = CreateOrganizationRequest.builder()
                .name("Neuro Forge")
                .slug("neuro-forge")
                .description("SDLC platform")
                .status(OrganizationStatus.ACTIVE)
                .build();

        organization = Organization.builder()
                .id(orgId)
                .name("Neuro Forge")
                .slug("neuro-forge")
                .description("SDLC platform")
                .status(OrganizationStatus.ACTIVE)
                .build();
    }

    @Test
    void createOrganization_Success() {
        when(organizationRepository.existsBySlug(request.getSlug())).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(organization);

        OrganizationResponse response = organizationService.createOrganization(request);

        assertNotNull(response);
        assertEquals(orgId, response.getId());
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getSlug(), response.getSlug());
        assertEquals(OrganizationStatus.ACTIVE, response.getStatus());

        verify(organizationRepository).existsBySlug(request.getSlug());
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void createOrganization_DuplicateSlug_ThrowsException() {
        when(organizationRepository.existsBySlug(request.getSlug())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> organizationService.createOrganization(request));

        verify(organizationRepository).existsBySlug(request.getSlug());
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void getAllOrganizations_Success() {
        when(organizationRepository.findAll()).thenReturn(Arrays.asList(organization));

        List<OrganizationResponse> list = organizationService.getAllOrganizations();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(orgId, list.get(0).getId());
        verify(organizationRepository).findAll();
    }

    @Test
    void getOrganizationById_Success() {
        doNothing().when(organizationAccessValidator).verifyOrganizationAccess(orgId);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

        OrganizationResponse response = organizationService.getOrganizationById(orgId);

        assertNotNull(response);
        assertEquals(orgId, response.getId());
        verify(organizationAccessValidator).verifyOrganizationAccess(orgId);
        verify(organizationRepository).findById(orgId);
    }

    @Test
    void getOrganizationById_AccessDenied_ThrowsAccessDeniedException() {
        doThrow(new AccessDeniedException("Access denied")).when(organizationAccessValidator).verifyOrganizationAccess(orgId);

        assertThrows(AccessDeniedException.class, () -> organizationService.getOrganizationById(orgId));

        verify(organizationAccessValidator).verifyOrganizationAccess(orgId);
        verify(organizationRepository, never()).findById(any(UUID.class));
    }

    @Test
    void getOrganizationById_NotFound_ThrowsException() {
        doNothing().when(organizationAccessValidator).verifyOrganizationAccess(orgId);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> organizationService.getOrganizationById(orgId));

        verify(organizationAccessValidator).verifyOrganizationAccess(orgId);
        verify(organizationRepository).findById(orgId);
    }

    @Test
    void updateOrganization_Success() {
        CreateOrganizationRequest updateReq = CreateOrganizationRequest.builder()
                .name("NeuroForge Pro")
                .slug("neuroforge-pro")
                .description("Updated description")
                .status(OrganizationStatus.INACTIVE)
                .build();

        Organization updatedOrg = Organization.builder()
                .id(orgId)
                .name("NeuroForge Pro")
                .slug("neuroforge-pro")
                .description("Updated description")
                .status(OrganizationStatus.INACTIVE)
                .build();

        doNothing().when(organizationAccessValidator).verifyOrganizationAccess(orgId);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(organizationRepository.existsBySlugAndIdNot(updateReq.getSlug(), orgId)).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(updatedOrg);

        OrganizationResponse response = organizationService.updateOrganization(orgId, updateReq);

        assertNotNull(response);
        assertEquals(updateReq.getName(), response.getName());
        assertEquals(updateReq.getSlug(), response.getSlug());
        assertEquals(OrganizationStatus.INACTIVE, response.getStatus());

        verify(organizationAccessValidator).verifyOrganizationAccess(orgId);
        verify(organizationRepository).findById(orgId);
        verify(organizationRepository).existsBySlugAndIdNot(updateReq.getSlug(), orgId);
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void updateOrganization_AccessDenied_ThrowsAccessDeniedException() {
        doThrow(new AccessDeniedException("Access denied")).when(organizationAccessValidator).verifyOrganizationAccess(orgId);

        assertThrows(AccessDeniedException.class, () -> organizationService.updateOrganization(orgId, request));

        verify(organizationAccessValidator).verifyOrganizationAccess(orgId);
        verify(organizationRepository, never()).findById(any(UUID.class));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_DuplicateSlug_ThrowsException() {
        doNothing().when(organizationAccessValidator).verifyOrganizationAccess(orgId);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(organizationRepository.existsBySlugAndIdNot(request.getSlug(), orgId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> organizationService.updateOrganization(orgId, request));

        verify(organizationAccessValidator).verifyOrganizationAccess(orgId);
        verify(organizationRepository).findById(orgId);
        verify(organizationRepository).existsBySlugAndIdNot(request.getSlug(), orgId);
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void deleteOrganization_Success() {
        doNothing().when(organizationAccessValidator).verifyOrganizationAccess(orgId);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        doNothing().when(organizationRepository).delete(organization);

        organizationService.deleteOrganization(orgId);

        verify(organizationAccessValidator).verifyOrganizationAccess(orgId);
        verify(organizationRepository).findById(orgId);
        verify(organizationRepository).delete(organization);
    }

    @Test
    void deleteOrganization_AccessDenied_ThrowsAccessDeniedException() {
        doThrow(new AccessDeniedException("Access denied")).when(organizationAccessValidator).verifyOrganizationAccess(orgId);

        assertThrows(AccessDeniedException.class, () -> organizationService.deleteOrganization(orgId));

        verify(organizationAccessValidator).verifyOrganizationAccess(orgId);
        verify(organizationRepository, never()).findById(any(UUID.class));
        verify(organizationRepository, never()).delete(any(Organization.class));
    }
}
