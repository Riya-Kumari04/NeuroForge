package com.neuroforge.backend.specification.controller;

import com.neuroforge.backend.specification.dto.common.ApiResponse;
import com.neuroforge.backend.specification.dto.common.ApiResponseBuilder;
import com.neuroforge.backend.specification.dto.request.CreateSpecificationRequest;
import com.neuroforge.backend.specification.dto.request.UpdateSpecificationRequest;
import com.neuroforge.backend.specification.dto.response.SpecificationResponse;
import com.neuroforge.backend.specification.enums.SpecificationStatus;
import com.neuroforge.backend.specification.service.SpecificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/specifications")
public class SpecificationController {

    private final SpecificationService specificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<SpecificationResponse>> createSpecification(
            @Valid @RequestBody CreateSpecificationRequest request) {

        log.info("Create Specification Request received | title={}", request.getTitle());

        SpecificationResponse response =
                specificationService.createSpecification(request);

        return ApiResponseBuilder.created(
                "Specification created successfully",
                response
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecificationResponse>> getSpecification(
            @PathVariable UUID id) {

        log.info("Fetch Specification Request received | id={}", id);

        SpecificationResponse response =
                specificationService.getSpecification(id);

        return ApiResponseBuilder.ok(
                "Specification fetched successfully",
                response
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SpecificationResponse>>> getSpecifications(

            @RequestParam(required = false) String title,

            @RequestParam(required = false) SpecificationStatus status,

            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt"
            )
            Pageable pageable) {

        log.info("Search Specifications Request received | title={} | status={} | page={} | size={}",
                title,
                status,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<SpecificationResponse> response =
                specificationService.getAllSpecifications(
                        title,
                        status,
                        pageable
                );

        return ApiResponseBuilder.ok(
                "Specifications fetched successfully",
                response
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecificationResponse>> updateSpecification(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSpecificationRequest request) {

        log.info("Update Specification Request received | id={}", id);

        SpecificationResponse response =
                specificationService.updateSpecification(id, request);

        return ApiResponseBuilder.ok(
                "Specification updated successfully",
                response
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSpecification(
            @PathVariable UUID id) {

        log.info("Delete Specification Request received | id={}", id);

        specificationService.deleteSpecification(id);

        return ApiResponseBuilder.ok(
                "Specification deleted successfully"
        );
    }

}
