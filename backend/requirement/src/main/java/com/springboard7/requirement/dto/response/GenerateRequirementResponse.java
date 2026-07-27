package com.springboard7.requirement.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateRequirementResponse {

    private String description;

    private List<String> userStories;

    private List<String> functionalRequirements;

    private List<String> nonFunctionalRequirements;

    private List<String> acceptanceCriteria;

    private String aiResponse;
}