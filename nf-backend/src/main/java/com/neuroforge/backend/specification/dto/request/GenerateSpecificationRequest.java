package com.neuroforge.backend.specification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSpecificationRequest {

    @NotBlank(message = "Prompt is required")
    private String prompt;

    private String projectId;
}
