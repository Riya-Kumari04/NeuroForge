package com.neuroforge.backend.specification.service.impl;

import com.neuroforge.backend.specification.dto.request.CreateSpecificationRequest;
import com.neuroforge.backend.specification.dto.request.UpdateSpecificationRequest;
import com.neuroforge.backend.specification.dto.response.SpecificationResponse;
import com.neuroforge.backend.specification.dto.response.SpecificationVersionResponse;
import com.neuroforge.backend.specification.entity.Specification;
import com.neuroforge.backend.specification.entity.SpecificationVersion;
import com.neuroforge.backend.specification.enums.SpecificationStatus;
import com.neuroforge.backend.specification.enums.VersionStatus;
import com.neuroforge.backend.specification.exception.BadRequestException;
import com.neuroforge.backend.specification.exception.DuplicateSpecificationException;
import com.neuroforge.backend.specification.exception.ResourceNotFoundException;
import com.neuroforge.backend.specification.exception.SpecificationNotFoundException;
import com.neuroforge.backend.specification.mapper.SpecificationMapper;
import com.neuroforge.backend.specification.repository.SpecificationRepository;
import com.neuroforge.backend.specification.service.SpecificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    private final SpecificationRepository specificationRepository;
    private final SpecificationMapper specificationMapper;

    @Override
    public SpecificationResponse createSpecification(CreateSpecificationRequest request) {

        String title = request.getTitle().trim();

        log.debug("Validating duplicate title: {}", title);

        validateDuplicateTitle(title);
        validateCreationRequest(request);

        Specification specification = buildSpecification(title);

        SpecificationVersion version = buildManualVersion(
                specification,
                request.getDescription(),
                request.getUserStories(),
                request.getAcceptanceCriteria(),
                request.getFunctionalRequirements(),
                request.getNonFunctionalRequirements(),
                1
        );

        specification.addVersion(version);

        specification = specificationRepository.save(specification);

        log.info("Specification created successfully | id={} | key={} | version={} | status={}",
                specification.getId(),
                specification.getSpecificationKey(),
                specification.getCurrentVersion(),
                specification.getStatus()
        );

        return specificationMapper.toResponse(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecificationResponse getSpecification(UUID specificationId) {

        Specification specification = getSpecificationOrThrow(specificationId);

        log.debug("Specification retrieved | id={} | version={} | status={}",
                specification.getId(),
                specification.getCurrentVersion(),
                specification.getStatus()
        );

        return specificationMapper.toResponse(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpecificationResponse> getAllSpecifications(
            String title,
            SpecificationStatus status,
            Pageable pageable) {

        Page<Specification> specifications;

        if (title != null && !title.isBlank() && status != null) {

            specifications = specificationRepository
                    .findByDeletedFalseAndTitleContainingIgnoreCaseAndStatus(
                            title.trim(),
                            status,
                            pageable
                    );

        } else if (title != null && !title.isBlank()) {

            specifications = specificationRepository
                    .findByDeletedFalseAndTitleContainingIgnoreCase(
                            title.trim(),
                            pageable
                    );

        } else if (status != null) {

            specifications = specificationRepository
                    .findByDeletedFalseAndStatus(
                            status,
                            pageable
                    );

        } else {

            specifications = specificationRepository
                    .findByDeletedFalse(pageable);

        }

        log.debug("Specification search completed | totalElements={} | totalPages={}",
                specifications.getTotalElements(),
                specifications.getTotalPages()
        );

        return specifications.map(specificationMapper::toResponse);
    }

    @Override
    public SpecificationResponse updateSpecification(
            UUID id,
            UpdateSpecificationRequest request) {

        Specification specification = getSpecificationOrThrow(id);

        int nextVersion = specification.getCurrentVersion() + 1;

        validateField(request.getDescription(), "Description");
        validateField(request.getUserStories(), "User Stories");
        validateField(request.getAcceptanceCriteria(), "Acceptance Criteria");
        validateField(request.getFunctionalRequirements(), "Functional Requirements");
        validateField(request.getNonFunctionalRequirements(), "Non Functional Requirements");

        SpecificationVersion version = buildManualVersion(
                specification,
                request.getDescription(),
                request.getUserStories(),
                request.getAcceptanceCriteria(),
                request.getFunctionalRequirements(),
                request.getNonFunctionalRequirements(),
                nextVersion
        );

        specification.addVersion(version);
        specification.setCurrentVersion(nextVersion);

        specification = specificationRepository.save(specification);

        log.info("Specification updated successfully | id={} | newVersion={} | status={}",
                specification.getId(),
                specification.getCurrentVersion(),
                specification.getStatus()
        );

        return specificationMapper.toResponse(specification);
    }

    @Override
    public void deleteSpecification(UUID id) {

        Specification specification = getSpecificationOrThrow(id);

        specification.setDeleted(true);

        specificationRepository.save(specification);

        log.info("Specification soft deleted successfully | id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecificationVersionResponse getApprovedVersion(UUID specificationId) {

        Specification specification =
                specificationRepository.findByIdAndDeletedFalse(specificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Specification not found"));

        SpecificationVersion version =
                specification.getVersions()
                        .stream()
                        .filter(v -> v.getStatus() == VersionStatus.APPROVED)
                        .max(Comparator.comparingInt(
                                SpecificationVersion::getVersionNumber))
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No approved version found"));

        return specificationMapper.toResponse(version);
    }

    private Specification buildSpecification(String title) {

        log.debug("Building new Specification entity");

        return Specification.builder()
                .specificationKey(generateSpecificationKey())
                .title(title)
                .currentVersion(1)
                .status(SpecificationStatus.DRAFT)
                .deleted(false)
                .build();
    }

    private SpecificationVersion buildManualVersion(
            Specification specification,
            String description,
            String userStories,
            String acceptanceCriteria,
            String functionalRequirements,
            String nonFunctionalRequirements,
            Integer versionNumber) {

        log.debug("Creating specification version | fromVersion={} | toVersion={}",
                versionNumber - 1,
                versionNumber
        );

        return SpecificationVersion.builder()
                .specification(specification)
                .versionNumber(versionNumber)
                .description(description != null ? description.trim() : "")
                .userStories(userStories != null ? userStories.trim() : null)
                .acceptanceCriteria(acceptanceCriteria != null ? acceptanceCriteria.trim() : null)
                .functionalRequirements(functionalRequirements != null ? functionalRequirements.trim() : null)
                .nonFunctionalRequirements(nonFunctionalRequirements != null ? nonFunctionalRequirements.trim() : null)
                .status(VersionStatus.DRAFT)
                .generatedBy("MANUAL")
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private Specification getSpecificationOrThrow(UUID id) {

        return specificationRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {

                    log.warn("Specification not found | id={}", id);

                    return new SpecificationNotFoundException(
                            "Specification not found with id : " + id
                    );
                });
    }

    private void validateDuplicateTitle(String title) {

        log.debug("Validating duplicate title: {}", title);

        if (specificationRepository.existsByTitleIgnoreCase(title)) {

            log.warn("Duplicate specification title found: {}", title);

            throw new DuplicateSpecificationException(
                    "Specification already exists with title : " + title
            );
        }

        log.debug("Title validation successful: {}", title);
    }

    private String generateSpecificationKey() {

        long count = specificationRepository.count() + 1;
        long sequence = (count * 1000L + (System.currentTimeMillis() % 1000L)) % 1000000L;

        String key = String.format("SPEC-%06d", sequence);

        log.debug("Generated specification key | key={}", key);

        return key;
    }

    private void validateCreationRequest(CreateSpecificationRequest request) {
        validateField(request.getDescription(), "Description");
        validateField(request.getUserStories(), "User Stories");
        validateField(request.getAcceptanceCriteria(), "Acceptance Criteria");
        validateField(request.getFunctionalRequirements(), "Functional Requirements");
        validateField(request.getNonFunctionalRequirements(), "Non Functional Requirements");
    }

    private void validateField(String value, String fieldName) {

        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required.");
        }
    }

}
