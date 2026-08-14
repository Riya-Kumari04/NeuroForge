package com.neuroforge.backend.specification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSpecificationResponse {

    private String title;
    private String description;
    private List<String> userStories;
    private List<String> acceptanceCriteria;
    private List<String> functionalRequirements;
    private List<String> nonFunctionalRequirements;
}
