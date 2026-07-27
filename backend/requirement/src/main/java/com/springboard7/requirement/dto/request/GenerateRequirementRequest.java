package com.springboard7.requirement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateRequirementRequest {

    @NotBlank(message = "Prompt is required")
    private String prompt;
}