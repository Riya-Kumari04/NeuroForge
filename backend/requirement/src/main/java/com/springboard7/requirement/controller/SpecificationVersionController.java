package com.springboard7.requirement.controller;

import com.springboard7.requirement.dto.common.ApiResponse;
import com.springboard7.requirement.dto.common.ApiResponseBuilder;
import com.springboard7.requirement.dto.request.RejectVersionRequest;
import com.springboard7.requirement.dto.request.UpdateVersionRequest;
import com.springboard7.requirement.dto.response.CompareVersionResponse;
import com.springboard7.requirement.dto.response.SpecificationVersionResponse;
import com.springboard7.requirement.logging.HttpLogMessages;
import com.springboard7.requirement.logging.LogMessages;
import com.springboard7.requirement.service.SpecificVersionService;
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

        log.info(LogMessages.VERSION_LIST_REQUEST, id);

        List<SpecificationVersionResponse> response =
                versionService.getVersions(id);

        log.info(
                LogMessages.VERSION_LIST_RESPONSE,
                id,
                response.size()
        );

        return ApiResponseBuilder.ok(
                "Versions fetched successfully",
                response
        );
    }

    @GetMapping("/{id}/versions/{version}")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> getVersion(
            @PathVariable UUID id,
            @PathVariable Integer version) {

        log.info(
                LogMessages.VERSION_FETCH_REQUEST,
                id,
                version
        );

        SpecificationVersionResponse response =
                versionService.getVersion(id, version);

        log.info(
                LogMessages.VERSION_FETCH_RESPONSE,
                id,
                version
        );

        return ApiResponseBuilder.ok(
                "Version fetched successfully",
                response
        );
    }

    @GetMapping("/{id}/latest")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> getLatestVersion(
            @PathVariable UUID id) {

        log.info(
                LogMessages.LATEST_VERSION_REQUEST,
                id
        );

        SpecificationVersionResponse response =
                versionService.getLatestVersion(id);

        log.info(
                LogMessages.LATEST_VERSION_RESPONSE,
                id,
                response.getVersionNumber()
        );

        return ApiResponseBuilder.ok(
                "Latest version fetched successfully",
                response
        );
    }

    @PostMapping("/{id}/versions/{version}/submit")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> submitForReview(
            @PathVariable UUID id,
            @PathVariable Integer version) {

        log.info(HttpLogMessages.SUBMIT_VERSION, id, version);

        SpecificationVersionResponse response =
                versionService.submitForReview(id, version);

        log.info(
                LogMessages.SUBMIT_REVIEW_RESPONSE,
                id,
                version,
                response.getStatus()
        );

        return ApiResponseBuilder.ok(
                "Version submitted for review successfully",
                response
        );
    }

    @PostMapping("/{id}/versions/{version}/approve")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> approveVersion(
            @PathVariable UUID id,
            @PathVariable Integer version) {

        log.info(HttpLogMessages.APPROVE_VERSION, id, version);

        SpecificationVersionResponse response =
                versionService.approveVersion(id, version);

        log.info(
                LogMessages.APPROVE_VERSION_RESPONSE,
                id,
                version,
                response.getStatus()
        );

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

        log.info(HttpLogMessages.REJECT_VERSION, id, version);

        SpecificationVersionResponse response =
                versionService.rejectVersion(id, version, request);

        log.info(
                LogMessages.REJECT_VERSION_RESPONSE,
                id,
                version,
                response.getStatus()
        );

        return ApiResponseBuilder.ok(
                "Version rejected successfully",
                response
        );
    }

    @PostMapping("/{id}/versions/{version}/archive")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> archiveVersion(
            @PathVariable UUID id,
            @PathVariable Integer version) {

        log.info(HttpLogMessages.ARCHIVE_VERSION, id, version);

        SpecificationVersionResponse response =
                versionService.archiveVersion(id, version);

        log.info(
                LogMessages.ARCHIVE_VERSION_RESPONSE,
                id,
                version,
                response.getStatus()
        );

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

        log.info(HttpLogMessages.COMPARE_VERSIONS, id, v1, v2);

        CompareVersionResponse response =
                versionService.compareVersions(id, v1, v2);

        log.info(
                LogMessages.COMPARE_VERSION_RESPONSE,
                id,
                v1,
                v2,
                response.getChanges().size()
        );

        return ApiResponseBuilder.ok(
                "Versions compared successfully",
                response
        );
    }

    @PutMapping("/{id}/versions/{version}")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>>
    updateDraftVersion(
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