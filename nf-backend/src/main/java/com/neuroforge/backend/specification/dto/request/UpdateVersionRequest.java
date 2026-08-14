package com.neuroforge.backend.specification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVersionRequest {

    @NotBlank(message = "Description is required")
    private String description;

    private String userStories;

    private String functionalRequirements;

    private String nonFunctionalRequirements;

    private String acceptanceCriteria;
}
