package com.springboard7.requirement.controller;

import com.springboard7.requirement.dto.common.ApiResponse;
import com.springboard7.requirement.dto.common.ApiResponseBuilder;
import com.springboard7.requirement.dto.request.CreateSpecificationRequest;
import com.springboard7.requirement.dto.request.UpdateSpecificationRequest;
import com.springboard7.requirement.dto.response.SpecificationResponse;
import com.springboard7.requirement.enums.SpecificationStatus;
import com.springboard7.requirement.logging.LogMessages;
import com.springboard7.requirement.service.SpecificationService;
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

        log.info(LogMessages.CREATE_REQUEST, request.getTitle());

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

        log.info(LogMessages.FETCH_REQUEST, id);

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

        log.info(
                LogMessages.SEARCH_REQUEST,
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

        log.info(LogMessages.UPDATE_REQUEST, id);

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

        log.info(LogMessages.DELETE_REQUEST, id);

        specificationService.deleteSpecification(id);

        return ApiResponseBuilder.ok(
                "Specification deleted successfully"
        );
    }


}