package com.springboard7.requirement.dto.request;

import com.springboard7.requirement.enums.CreationMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UpdateSpecificationRequest {

    @NotNull(message = "Creation mode is required.")
    private CreationMode creationMode;

    private String prompt;

    private String description;

    private String userStories;

    private String acceptanceCriteria;

    private String functionalRequirements;

    private String nonFunctionalRequirements;

}