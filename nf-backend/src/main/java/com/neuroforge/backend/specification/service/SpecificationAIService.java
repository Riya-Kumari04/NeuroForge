package com.neuroforge.backend.specification.service;

import com.neuroforge.backend.specification.dto.request.GenerateSpecificationRequest;
import com.neuroforge.backend.specification.dto.response.GenerateSpecificationResponse;

public interface SpecificationAIService {

    GenerateSpecificationResponse generateSpecification(GenerateSpecificationRequest request);
}
