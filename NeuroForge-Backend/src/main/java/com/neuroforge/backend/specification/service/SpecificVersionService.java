package com.neuroforge.backend.specification.service;

import com.neuroforge.backend.specification.dto.request.RejectVersionRequest;
import com.neuroforge.backend.specification.dto.request.UpdateVersionRequest;
import com.neuroforge.backend.specification.dto.response.CompareVersionResponse;
import com.neuroforge.backend.specification.dto.response.SpecificationVersionResponse;

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
