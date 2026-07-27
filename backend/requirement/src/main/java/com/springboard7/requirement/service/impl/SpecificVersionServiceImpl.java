package com.springboard7.requirement.service.impl;

import com.springboard7.requirement.dto.request.RejectVersionRequest;
import com.springboard7.requirement.dto.request.UpdateVersionRequest;
import com.springboard7.requirement.dto.response.CompareVersionResponse;
import com.springboard7.requirement.dto.response.FieldChangeResponse;
import com.springboard7.requirement.dto.response.SpecificationVersionResponse;
import com.springboard7.requirement.entity.Specification;
import com.springboard7.requirement.entity.SpecificationVersion;
import com.springboard7.requirement.enums.VersionStatus;
import com.springboard7.requirement.exception.InvalidWorkflowStateException;
import com.springboard7.requirement.exception.SpecificationNotFoundException;
import com.springboard7.requirement.logging.CompareFields;
import com.springboard7.requirement.logging.LogMessages;
import com.springboard7.requirement.mapper.SpecificationMapper;
import com.springboard7.requirement.repository.SpecificationVersionRepository;
import com.springboard7.requirement.service.SpecificVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SpecificVersionServiceImpl implements SpecificVersionService {

    private final SpecificationVersionRepository versionRepository;
    private final SpecificationMapper specificationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SpecificationVersionResponse> getVersions(UUID specificationId) {

        log.debug(LogMessages.VERSION_LIST_REQUEST, specificationId);

        List<SpecificationVersionResponse> versions = versionRepository
                .findBySpecificationIdOrderByVersionNumberDesc(specificationId)
                .stream()
                .map(specificationMapper::toResponse)
                .toList();

        log.debug(
                LogMessages.VERSION_LIST_RESPONSE,
                specificationId,
                versions.size()
        );

        return versions;
    }

    @Override
    @Transactional(readOnly = true)
    public SpecificationVersionResponse getVersion(
            UUID specificationId,
            Integer versionNumber) {

        log.debug(
                LogMessages.VERSION_FETCH_REQUEST,
                specificationId,
                versionNumber
        );

        SpecificationVersion version =
                getVersionOrThrow(specificationId, versionNumber);

        log.debug(
                LogMessages.VERSION_FETCH_RESPONSE,
                specificationId,
                versionNumber
        );

        return specificationMapper.toResponse(version);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecificationVersionResponse getLatestVersion(UUID specificationId) {

        log.debug(
                LogMessages.LATEST_VERSION_REQUEST,
                specificationId
        );

        SpecificationVersion version = versionRepository
                .findTopBySpecificationIdOrderByVersionNumberDesc(specificationId)
                .orElseThrow(() -> {

                    log.warn(LogMessages.SPEC_NOT_FOUND, specificationId);

                    return new SpecificationNotFoundException(
                            "No version found for specification : " + specificationId
                    );
                });

        log.debug(
                LogMessages.LATEST_VERSION_RESPONSE,
                specificationId,
                version.getVersionNumber()
        );

        return specificationMapper.toResponse(version);
    }

    @Override
    public SpecificationVersionResponse submitForReview(
            UUID specificationId,
            Integer versionNumber) {
        log.debug(
                LogMessages.SUBMIT_REVIEW_REQUEST,
                specificationId,
                versionNumber
        );

        SpecificationVersion version =
                getVersionOrThrow(specificationId, versionNumber);

        validateStatus(
                version,
                VersionStatus.DRAFT,
                "submitted for review"
        );

        version.setStatus(VersionStatus.IN_REVIEW);
        if (version.getSpecification() != null) {
            version.getSpecification().setStatus(com.springboard7.requirement.enums.SpecificationStatus.IN_REVIEW);
        }

        version = versionRepository.save(version);

        log.info(
                LogMessages.SUBMIT_REVIEW_RESPONSE,
                specificationId,
                versionNumber,
                version.getStatus()
        );
        return specificationMapper.toResponse(version);
    }

    @Override
    public SpecificationVersionResponse approveVersion(
            UUID specificationId,
            Integer versionNumber) {

        log.debug(
                LogMessages.APPROVE_VERSION_REQUEST,
                specificationId,
                versionNumber
        );

        SpecificationVersion version =
                getVersionOrThrow(specificationId, versionNumber);

        validateStatus(
                version,
                VersionStatus.IN_REVIEW,
                "approved"
        );

        version.setStatus(VersionStatus.APPROVED);
        if (version.getSpecification() != null) {
            version.getSpecification().setStatus(com.springboard7.requirement.enums.SpecificationStatus.APPROVED);
        }
        version.setApprovedAt(LocalDateTime.now());

        // Later after Auth Service integration
        // version.setApprovedBy(SecurityUtils.getCurrentUsername());

        version = versionRepository.save(version);

        log.info(
                LogMessages.APPROVE_VERSION_RESPONSE,
                specificationId,
                versionNumber,
                version.getStatus()
        );
        return specificationMapper.toResponse(version);
    }

    @Override
    public SpecificationVersionResponse rejectVersion(
            UUID specificationId,
            Integer versionNumber,
            RejectVersionRequest request) {

        log.debug(
                LogMessages.REJECT_VERSION_REQUEST,
                specificationId,
                versionNumber
        );

        SpecificationVersion version =
                getVersionOrThrow(specificationId, versionNumber);

        validateStatus(
                version,
                VersionStatus.IN_REVIEW,
                "rejected"
        );

        version.setStatus(VersionStatus.REJECTED);
        if (version.getSpecification() != null) {
            version.getSpecification().setStatus(com.springboard7.requirement.enums.SpecificationStatus.REJECTED);
        }
        version.setReviewedAt(LocalDateTime.now());
        version.setReviewComments(request.getComment());

        // Later
        // version.setReviewedBy(SecurityUtils.getCurrentUsername());

        version = versionRepository.save(version);

        log.info(
                LogMessages.REJECT_VERSION_RESPONSE,
                specificationId,
                versionNumber,
                version.getStatus()
        );
        return specificationMapper.toResponse(version);
    }

    @Override
    public SpecificationVersionResponse archiveVersion(
            UUID specificationId,
            Integer versionNumber) {

        log.debug(
                LogMessages.ARCHIVE_VERSION_REQUEST,
                specificationId,
                versionNumber
        );

        SpecificationVersion version =
                getVersionOrThrow(specificationId, versionNumber);

        validateStatus(
                version,
                VersionStatus.APPROVED,
                "archived"
        );

        version.setStatus(VersionStatus.ARCHIVED);
        if (version.getSpecification() != null) {
            version.getSpecification().setStatus(com.springboard7.requirement.enums.SpecificationStatus.ARCHIVED);
        }

        version = versionRepository.save(version);

        log.info(
                LogMessages.ARCHIVE_VERSION_RESPONSE,
                specificationId,
                versionNumber,
                version.getStatus()
        );
        return specificationMapper.toResponse(version);
    }

    @Override
    @Transactional(readOnly = true)
    public CompareVersionResponse compareVersions(
            UUID specificationId,
            Integer version1,
            Integer version2) {

        log.debug(
                LogMessages.COMPARE_VERSION_REQUEST,
                specificationId,
                version1,
                version2
        );

        SpecificationVersion oldVersion =
                getVersionOrThrow(specificationId, version1);

        SpecificationVersion newVersion =
                getVersionOrThrow(specificationId, version2);

        List<FieldChangeResponse> changes = new ArrayList<>();

        compareField(
                changes,
                CompareFields.DESCRIPTION,
                oldVersion.getDescription(),
                newVersion.getDescription()
        );

        compareField(
                changes,
                CompareFields.USER_STORY,
                oldVersion.getUserStories(),
                newVersion.getUserStories()
        );

        compareField(
                changes,
                CompareFields.ACCEPTANCE_CRITERIA,
                oldVersion.getAcceptanceCriteria(),
                newVersion.getAcceptanceCriteria()
        );

        compareField(
                changes,
                CompareFields.FUNCTIONAL_REQUIREMENTS,
                oldVersion.getFunctionalRequirements(),
                newVersion.getFunctionalRequirements()
        );

        compareField(
                changes,
                CompareFields.NON_FUNCTIONAL_REQUIREMENTS,
                oldVersion.getNonFunctionalRequirements(),
                newVersion.getNonFunctionalRequirements()
        );

        compareField(
                changes,
                CompareFields.AI_PROMPT,
                oldVersion.getAiPrompt(),
                newVersion.getAiPrompt()
        );

        compareField(
                changes,
                CompareFields.AI_RESPONSE,
                oldVersion.getAiResponse(),
                newVersion.getAiResponse()
        );

        log.info(
                LogMessages.COMPARE_VERSION_RESPONSE,
                specificationId,
                version1,
                version2,
                changes.size()
        );

        return CompareVersionResponse.builder()
                .version1(version1)
                .version2(version2)
                .changes(changes)
                .build();
    }



    @Override
    @Transactional(readOnly = true)
    public String getVersionStatus(
            UUID specificationId,
            Integer versionNumber) {

        SpecificationVersion version =
                getVersionOrThrow(
                        specificationId,
                        versionNumber
                );

        return version.getStatus().name();
    }



    private SpecificationVersion getVersionOrThrow(
            UUID specificationId,
            Integer versionNumber) {

        return versionRepository
                .findBySpecificationIdAndVersionNumber(
                        specificationId,
                        versionNumber
                )
                .orElseThrow(() -> {

                    log.warn(
                            "Specification Version Not Found | specificationId={} | version={}",
                            specificationId,
                            versionNumber
                    );

                    return new SpecificationNotFoundException(
                            String.format(
                                    "Version %d not found for specification %s",
                                    versionNumber,
                                    specificationId
                            )
                    );
                });
    }

    private void validateStatus(
            SpecificationVersion version,
            VersionStatus expected,
            String action) {

        log.debug(
                LogMessages.WORKFLOW_STATUS_VALIDATION,
                expected,
                version.getStatus(),
                action
        );

        if (version.getStatus() != expected) {

            log.warn(
                    LogMessages.INVALID_WORKFLOW_STATE,
                    expected,
                    version.getStatus(),
                    action
            );

            throw new InvalidWorkflowStateException(
                    String.format(
                            "Only %s versions can be %s.",
                            expected,
                            action
                    )
            );
        }
    }

    private void compareField(
            List<FieldChangeResponse> changes,
            String field,
            String oldValue,
            String newValue) {

        if (!Objects.equals(oldValue, newValue)) {

            changes.add(
                    FieldChangeResponse.builder()
                            .field(field)
                            .oldValue(oldValue)
                            .newValue(newValue)
                            .build()
            );

        }
    }

    @Override
    public SpecificationVersionResponse updateDraftVersion(
            UUID specificationId,
            Integer versionNumber,
            UpdateVersionRequest request) {

        log.debug(
                LogMessages.UPDATE_VERSION_REQUEST,
                specificationId,
                versionNumber
        );

        SpecificationVersion version =
                getVersionOrThrow(specificationId, versionNumber);

        validateStatus(
                version,
                VersionStatus.DRAFT,
                "edited"
        );

        version.setDescription(request.getDescription());
        version.setUserStories(request.getUserStories());
        version.setFunctionalRequirements(
                request.getFunctionalRequirements()
        );
        version.setNonFunctionalRequirements(
                request.getNonFunctionalRequirements()
        );
        version.setAcceptanceCriteria(
                request.getAcceptanceCriteria()
        );

        version = versionRepository.save(version);

        log.info(
                LogMessages.UPDATE_VERSION_RESPONSE,
                specificationId,
                versionNumber
        );

        return specificationMapper.toResponse(version);
    }

}