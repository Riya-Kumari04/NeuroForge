package com.neuroforge.backend.specification.controller;

import com.neuroforge.backend.specification.dto.common.ApiResponse;
import com.neuroforge.backend.specification.dto.common.ApiResponseBuilder;
import com.neuroforge.backend.specification.dto.request.RejectVersionRequest;
import com.neuroforge.backend.specification.dto.request.UpdateVersionRequest;
import com.neuroforge.backend.specification.dto.response.CompareVersionResponse;
import com.neuroforge.backend.specification.dto.response.SpecificationVersionResponse;
import com.neuroforge.backend.specification.service.SpecificVersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/specifications")
public class SpecificationVersionController {

    private final SpecificVersionService versionService;

    @GetMapping("/{id}/versions")
    public ResponseEntity<ApiResponse<List<SpecificationVersionResponse>>> getVersions(
            @PathVariable UUID id) {

        log.info("Fetch Version List Request | specificationId={}", id);

        List<SpecificationVersionResponse> response =
                versionService.getVersions(id);

        log.info("Version List fetched | specificationId={} | count={}", id, response.size());

        return ApiResponseBuilder.ok(
                "Versions fetched successfully",
                response
        );
    }

    @GetMapping("/{id}/versions/{version}")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> getVersion(
            @PathVariable UUID id,
            @PathVariable Integer version) {

        log.info("Fetch Specification Version Request | specificationId={} | version={}", id, version);

        SpecificationVersionResponse response =
                versionService.getVersion(id, version);

        log.info("Specification Version fetched | specificationId={} | version={}", id, version);

        return ApiResponseBuilder.ok(
                "Version fetched successfully",
                response
        );
    }

    @GetMapping("/{id}/latest")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> getLatestVersion(
            @PathVariable UUID id) {

        log.info("Fetch Latest Specification Version Request | specificationId={}", id);

        SpecificationVersionResponse response =
                versionService.getLatestVersion(id);

        log.info("Latest Specification Version fetched | specificationId={} | version={}", id, response.getVersionNumber());

        return ApiResponseBuilder.ok(
                "Latest version fetched successfully",
                response
        );
    }

    @PostMapping("/{id}/versions/{version}/submit")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> submitForReview(
            @PathVariable UUID id,
            @PathVariable Integer version) {

        log.info("HTTP Request received | Submit Version for Review | specificationId={} | version={}", id, version);

        SpecificationVersionResponse response =
                versionService.submitForReview(id, version);

        log.info("Version submitted for review | specificationId={} | version={} | status={}", id, version, response.getStatus());

        return ApiResponseBuilder.ok(
                "Version submitted for review successfully",
                response
        );
    }

    @PostMapping("/{id}/versions/{version}/approve")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> approveVersion(
            @PathVariable UUID id,
            @PathVariable Integer version) {

        log.info("HTTP Request received | Approve Specification Version | specificationId={} | version={}", id, version);

        SpecificationVersionResponse response =
                versionService.approveVersion(id, version);

        log.info("Version approved successfully | specificationId={} | version={} | status={}", id, version, response.getStatus());

        return ApiResponseBuilder.ok(
                "Version approved successfully",
                response
        );
    }

    @PostMapping("/{id}/versions/{version}/reject")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> rejectVersion(
            @PathVariable UUID id,
            @PathVariable Integer version,
            @Valid @RequestBody RejectVersionRequest request) {

        log.info("HTTP Request received | Reject Specification Version | specificationId={} | version={}", id, version);

        SpecificationVersionResponse response =
                versionService.rejectVersion(id, version, request);

        log.info("Version rejected successfully | specificationId={} | version={} | status={}", id, version, response.getStatus());

        return ApiResponseBuilder.ok(
                "Version rejected successfully",
                response
        );
    }

    @PostMapping("/{id}/versions/{version}/archive")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> archiveVersion(
            @PathVariable UUID id,
            @PathVariable Integer version) {

        log.info("HTTP Request received | Archive Specification Version | specificationId={} | version={}", id, version);

        SpecificationVersionResponse response =
                versionService.archiveVersion(id, version);

        log.info("Version archived successfully | specificationId={} | version={} | status={}", id, version, response.getStatus());

        return ApiResponseBuilder.ok(
                "Version archived successfully",
                response
        );
    }

    @GetMapping("/{id}/versions/{v1}/compare/{v2}")
    public ResponseEntity<ApiResponse<CompareVersionResponse>> compareVersions(
            @PathVariable UUID id,
            @PathVariable Integer v1,
            @PathVariable Integer v2) {

        log.info("HTTP Request received | Compare Specification Versions | specificationId={} | v1={} | v2={}", id, v1, v2);

        CompareVersionResponse response =
                versionService.compareVersions(id, v1, v2);

        log.info("Versions compared successfully | specificationId={} | v1={} | v2={} | diffCount={}", id, v1, v2, response.getChanges().size());

        return ApiResponseBuilder.ok(
                "Versions compared successfully",
                response
        );
    }

    @PutMapping("/{id}/versions/{version}")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> updateDraftVersion(
            @PathVariable UUID id,
            @PathVariable Integer version,
            @Valid @RequestBody UpdateVersionRequest request) {

        SpecificationVersionResponse response =
                versionService.updateDraftVersion(
                        id,
                        version,
                        request
                );

        return ApiResponseBuilder.ok(
                "Draft version updated successfully",
                response
        );
    }

}
