package com.springboard7.requirement.service;

import com.springboard7.requirement.dto.request.RejectVersionRequest;
import com.springboard7.requirement.dto.request.UpdateVersionRequest;
import com.springboard7.requirement.dto.response.CompareVersionResponse;
import com.springboard7.requirement.dto.response.SpecificationVersionResponse;

import java.util.List;
import java.util.UUID;

public interface SpecificVersionService {

    List<SpecificationVersionResponse> getVersions(UUID specificationId);

    SpecificationVersionResponse getVersion(
            UUID specificationId,
            Integer versionNumber
    );

    SpecificationVersionResponse getLatestVersion(UUID specificationId);

    SpecificationVersionResponse submitForReview(
            UUID specificationId,
            Integer versionNumber
    );

    SpecificationVersionResponse approveVersion(
            UUID specificationId,
            Integer versionNumber
    );

    SpecificationVersionResponse rejectVersion(
            UUID specificationId,
            Integer versionNumber,
            RejectVersionRequest request
    );

    SpecificationVersionResponse archiveVersion(
            UUID specificationId,
            Integer versionNumber
    );

    CompareVersionResponse compareVersions(
            UUID specificationId,
            Integer version1,
            Integer version2
    );

    SpecificationVersionResponse updateDraftVersion(
            UUID specificationId,
            Integer versionNumber,
            UpdateVersionRequest request);


    String getVersionStatus(UUID specificationId,
                            Integer versionNumber);

}
