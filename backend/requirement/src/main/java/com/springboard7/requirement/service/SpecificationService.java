package com.springboard7.requirement.service;

import com.springboard7.requirement.dto.request.CreateSpecificationRequest;
import com.springboard7.requirement.dto.request.GenerateRequirementRequest;
import com.springboard7.requirement.dto.request.UpdateSpecificationRequest;
import com.springboard7.requirement.dto.response.SpecificationResponse;
import com.springboard7.requirement.dto.response.SpecificationVersionResponse;
import com.springboard7.requirement.enums.SpecificationStatus;
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


}