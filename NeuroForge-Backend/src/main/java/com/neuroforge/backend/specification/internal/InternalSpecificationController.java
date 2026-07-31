package com.neuroforge.backend.specification.internal;

import com.neuroforge.backend.specification.dto.common.ApiResponse;
import com.neuroforge.backend.specification.dto.common.ApiResponseBuilder;
import com.neuroforge.backend.specification.dto.response.SpecificationVersionResponse;
import com.neuroforge.backend.specification.service.SpecificVersionService;
import com.neuroforge.backend.specification.service.SpecificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/specifications")
public class InternalSpecificationController {

    private final SpecificVersionService versionService;
    private final SpecificationService specificationService;

    @GetMapping("/{id}/approved")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> getApprovedVersion(
            @PathVariable UUID id) {

        return ApiResponseBuilder.ok(
                "Approved specification fetched successfully",
                specificationService.getApprovedVersion(id)
        );
    }

    @GetMapping("/{id}/versions/{version}")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> getVersion(
            @PathVariable UUID id,
            @PathVariable Integer version) {

        return ApiResponseBuilder.ok(
                "Version fetched successfully",
                versionService.getVersion(id, version)
        );
    }

    @GetMapping("/{id}/latest")
    public ResponseEntity<ApiResponse<SpecificationVersionResponse>> getLatestVersion(
            @PathVariable UUID id) {

        return ApiResponseBuilder.ok(
                "Latest version fetched successfully",
                versionService.getLatestVersion(id)
        );
    }

    @GetMapping("/{id}/versions/{version}/status")
    public ResponseEntity<ApiResponse<String>> getVersionStatus(
            @PathVariable UUID id,
            @PathVariable Integer version) {

        return ApiResponseBuilder.ok(
                "Version status fetched successfully",
                versionService.getVersionStatus(id, version)
        );
    }
}
