package com.springboard7.requirement.service.impl;

import com.springboard7.requirement.dto.request.CreateSpecificationRequest;
import com.springboard7.requirement.dto.request.GenerateRequirementRequest;
import com.springboard7.requirement.dto.request.UpdateSpecificationRequest;
import com.springboard7.requirement.dto.response.GenerateRequirementResponse;
import com.springboard7.requirement.dto.response.SpecificationResponse;
import com.springboard7.requirement.dto.response.SpecificationVersionResponse;
import com.springboard7.requirement.entity.Specification;
import com.springboard7.requirement.entity.SpecificationVersion;
import com.springboard7.requirement.enums.CreationMode;
import com.springboard7.requirement.enums.SpecificationStatus;
import com.springboard7.requirement.enums.VersionStatus;
import com.springboard7.requirement.exception.BadRequestException;
import com.springboard7.requirement.exception.DuplicateSpecificationException;
import com.springboard7.requirement.exception.ResourceNotFoundException;
import com.springboard7.requirement.exception.SpecificationNotFoundException;
import com.springboard7.requirement.feign.AIServiceClient;
import com.springboard7.requirement.logging.LogMessages;
import com.springboard7.requirement.mapper.AIRequirementMapper;
import com.springboard7.requirement.mapper.SpecificationMapper;
import com.springboard7.requirement.repository.SpecificationRepository;
import com.springboard7.requirement.service.SpecificationService;
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
    private final AIServiceClient aiServiceClient;
    private final AIRequirementMapper aiRequirementMapper;

    @Override
    public SpecificationResponse createSpecification(CreateSpecificationRequest request) {

        String title = request.getTitle().trim();

        log.debug(LogMessages.TITLE_VALIDATION, title);

        validateDuplicateTitle(title);
        validateCreationRequest(request);

        Specification specification = buildSpecification(title);

        SpecificationVersion version;

        if (request.getCreationMode() == CreationMode.AI) {

            GenerateRequirementRequest aiRequest =
                    GenerateRequirementRequest.builder()
                            .prompt(request.getPrompt().trim())
                            .build();

            GenerateRequirementResponse aiResponse =
                    aiServiceClient.generateRequirements(aiRequest);

            version = buildAIVersion(
                    specification,
                    aiResponse,
                    request.getPrompt().trim(),
                    1
            );

        } else {

            version = buildManualVersion(
                    specification,
                    request.getDescription(),
                    request.getUserStories(),
                    request.getAcceptanceCriteria(),
                    request.getFunctionalRequirements(),
                    request.getNonFunctionalRequirements(),
                    1
            );
        }

        specification.addVersion(version);

        specification = specificationRepository.save(specification);

        log.info(
                LogMessages.SPEC_CREATED,
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

        log.debug(
                LogMessages.SPEC_FOUND,
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

        log.debug(
                LogMessages.SEARCH_COMPLETED,
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

        SpecificationVersion version;

        if (request.getCreationMode() == CreationMode.AI) {

            validateField(request.getPrompt(), "Prompt");

            GenerateRequirementRequest aiRequest =
                    GenerateRequirementRequest.builder()
                            .prompt(request.getPrompt().trim())
                            .build();

            GenerateRequirementResponse aiResponse =
                    aiServiceClient.generateRequirements(aiRequest);

            version = buildAIVersion(
                    specification,
                    aiResponse,
                    request.getPrompt().trim(),
                    nextVersion
            );

        } else {

            validateField(request.getDescription(), "Description");
            validateField(request.getUserStories(), "User Stories");
            validateField(request.getAcceptanceCriteria(), "Acceptance Criteria");
            validateField(request.getFunctionalRequirements(), "Functional Requirements");
            validateField(request.getNonFunctionalRequirements(), "Non Functional Requirements");

            version = buildManualVersion(
                    specification,
                    request.getDescription(),
                    request.getUserStories(),
                    request.getAcceptanceCriteria(),
                    request.getFunctionalRequirements(),
                    request.getNonFunctionalRequirements(),
                    nextVersion
            );
        }

        specification.addVersion(version);
        specification.setCurrentVersion(nextVersion);

        specification = specificationRepository.save(specification);

        log.info(
                LogMessages.SPEC_UPDATED,
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

        log.info(LogMessages.SPEC_DELETED, id);
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

    private SpecificationVersion buildAIVersion(
            Specification specification,
            GenerateRequirementResponse aiResponse,
            String prompt,
            Integer versionNumber) {

        log.debug(
                LogMessages.VERSION_CREATION,
                versionNumber - 1,
                versionNumber
        );

        return aiRequirementMapper.toEntity(
                aiResponse,
                specification,
                versionNumber,
                prompt
        );
    }

    private Specification buildSpecification(String title) {

        log.debug(LogMessages.BUILD_SPEC);

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

        log.debug(
                LogMessages.VERSION_CREATION,
                versionNumber - 1,
                versionNumber
        );

        return SpecificationVersion.builder()
                .specification(specification)
                .versionNumber(versionNumber)
                .description(description.trim())
                .userStories(userStories.trim())
                .acceptanceCriteria(acceptanceCriteria.trim())
                .functionalRequirements(functionalRequirements.trim())
                .nonFunctionalRequirements(nonFunctionalRequirements.trim())
                .status(VersionStatus.DRAFT)
                .generatedBy("MANUAL")
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private Specification getSpecificationOrThrow(UUID id) {

        return specificationRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {

                    log.warn(LogMessages.SPEC_NOT_FOUND, id);

                    return new SpecificationNotFoundException(
                            "Specification not found with id : " + id
                    );
                });
    }

    private void validateDuplicateTitle(String title) {

        log.debug(LogMessages.TITLE_VALIDATION, title);

        if (specificationRepository.existsByTitleIgnoreCase(title)) {

            log.warn(LogMessages.DUPLICATE_TITLE, title);

            throw new DuplicateSpecificationException(
                    "Specification already exists with title : " + title
            );
        }

        log.debug(LogMessages.TITLE_VALIDATION_SUCCESS, title);
    }

    private String generateSpecificationKey() {

        Long sequence = specificationRepository.getNextSpecificationSequence();

        String key = String.format("SPEC-%06d", sequence);

        log.debug(LogMessages.KEY_GENERATED, key);

        return key;
    }

    private void validateCreationRequest(CreateSpecificationRequest request) {

        if (request.getCreationMode() == CreationMode.AI) {

            validateField(request.getPrompt(), "Prompt");
            return;
        }

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