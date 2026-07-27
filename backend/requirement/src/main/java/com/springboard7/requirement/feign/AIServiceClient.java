package com.springboard7.requirement.feign;

import com.springboard7.requirement.dto.request.GenerateRequirementRequest;
import com.springboard7.requirement.dto.response.GenerateRequirementResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "AI-SERVICE",
        fallbackFactory = AIServiceFallbackFactory.class)
public interface AIServiceClient {

    @PostMapping("/api/v1/ai/generate")
    GenerateRequirementResponse generateRequirements(
            @RequestBody GenerateRequirementRequest request
    );

}