package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CreateOrganizationRequest;
import com.neuroforge.backend.dto.OrganizationResponse;
import com.neuroforge.backend.entity.Organization;
import com.neuroforge.backend.entity.OrganizationStatus;
import com.neuroforge.backend.exception.DuplicateResourceException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.OrganizationRepository;
import com.neuroforge.backend.security.OrganizationAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationAccessValidator organizationAccessValidator;

    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        if (organizationRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Organization slug already exists: " + request.getSlug());
        }

        OrganizationStatus status = request.getStatus() != null ? request.getStatus() : OrganizationStatus.ACTIVE;

        Organization organization = Organization.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .status(status)
                .build();

        Organization saved = organizationRepository.save(organization);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(UUID id) {
        organizationAccessValidator.verifyOrganizationAccess(id);
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));
        return mapToResponse(organization);
    }

    @Transactional
    public OrganizationResponse updateOrganization(UUID id, CreateOrganizationRequest request) {
        organizationAccessValidator.verifyOrganizationAccess(id);
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));

        if (organizationRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            throw new DuplicateResourceException("Organization slug already exists: " + request.getSlug());
        }

        organization.setName(request.getName());
        organization.setSlug(request.getSlug());
        organization.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            organization.setStatus(request.getStatus());
        }

        Organization updated = organizationRepository.save(organization);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteOrganization(UUID id) {
        organizationAccessValidator.verifyOrganizationAccess(id);
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));
        organizationRepository.delete(organization);
    }

    private OrganizationResponse mapToResponse(Organization org) {
        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .description(org.getDescription())
                .status(org.getStatus())
                .createdAt(org.getCreatedAt())
                .createdBy(org.getCreatedBy())
                .updatedAt(org.getUpdatedAt())
                .updatedBy(org.getUpdatedBy())
                .build();
    }
}
