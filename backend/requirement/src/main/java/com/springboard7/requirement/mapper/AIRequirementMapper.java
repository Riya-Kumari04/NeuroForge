package com.springboard7.requirement.mapper;

import com.springboard7.requirement.dto.response.GenerateRequirementResponse;
import com.springboard7.requirement.entity.Specification;
import com.springboard7.requirement.entity.SpecificationVersion;
import com.springboard7.requirement.enums.VersionStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AIRequirementMapper {

    public SpecificationVersion toEntity(
            GenerateRequirementResponse response,
            Specification specification,
            Integer versionNumber,
            String aiPrompt) {
        return SpecificationVersion.builder()
                .specification(specification)
                .versionNumber(versionNumber)
                .description(response.getDescription())
                .userStories(toText(response.getUserStories()))
                .functionalRequirements(toText(response.getFunctionalRequirements()))
                .nonFunctionalRequirements(toText(response.getNonFunctionalRequirements()))
                .acceptanceCriteria(toText(response.getAcceptanceCriteria()))
                .aiPrompt(aiPrompt)
                .aiResponse(response.getAiResponse())
                .status(VersionStatus.DRAFT)
                .generatedAt(LocalDateTime.now())
                .generatedBy("AI")
                .build();
    }

    private String toText(List<String> items) {

        if (items == null || items.isEmpty()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < items.size(); i++) {

            builder.append(i + 1)
                    .append(". ")
                    .append(items.get(i));

            if (i < items.size() - 1) {
                builder.append(System.lineSeparator())
                        .append(System.lineSeparator());
            }
        }

        return builder.toString();
    }
}