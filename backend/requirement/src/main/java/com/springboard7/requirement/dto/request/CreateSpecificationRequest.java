package com.springboard7.requirement.dto.request;

import com.springboard7.requirement.enums.CreationMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSpecificationRequest {

    @NotBlank(message = "Title is required.")
    private String title;

    @NotNull(message = "Creation mode is required.")
    private CreationMode creationMode;

    private String prompt;

    private String description;

    private String userStories;

    private String acceptanceCriteria;

    private String functionalRequirements;

    private String nonFunctionalRequirements;

}