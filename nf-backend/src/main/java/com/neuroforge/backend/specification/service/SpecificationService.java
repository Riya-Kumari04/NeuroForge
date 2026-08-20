package com.neuroforge.backend.specification.service;

import com.neuroforge.backend.specification.dto.request.CreateSpecificationRequest;
import com.neuroforge.backend.specification.dto.request.SaveAISpecificationRequest;
import com.neuroforge.backend.specification.dto.request.UpdateSpecificationRequest;
import com.neuroforge.backend.specification.dto.response.SpecificationResponse;
import com.neuroforge.backend.specification.dto.response.SpecificationVersionResponse;
import com.neuroforge.backend.specification.enums.SpecificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SpecificationService {

    SpecificationResponse createSpecification(CreateSpecificationRequest request);

    SpecificationResponse getSpecification(UUID specificationId);

    Page<SpecificationResponse> getAllSpecifications(
            String title,
            SpecificationStatus status,
            Pageable pageable
    );

    SpecificationResponse updateSpecification(
            UUID id,
            UpdateSpecificationRequest request
    );

    void deleteSpecification(UUID id);

    SpecificationVersionResponse getApprovedVersion(UUID specificationId);

    // Module 4: Save AI-generated specification
    SpecificationResponse saveAISpecification(SaveAISpecificationRequest request);

}
