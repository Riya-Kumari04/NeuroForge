package com.neuroforge.backend.specification.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSpecificationRequest {

    private String description;

    private String userStories;

    private String acceptanceCriteria;

    private String functionalRequirements;

    private String nonFunctionalRequirements;

}
