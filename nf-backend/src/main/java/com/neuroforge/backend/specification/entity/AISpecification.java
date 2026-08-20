package com.neuroforge.backend.specification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_specifications")
public class AISpecification {

    @Id
    private String id;

    @Indexed
    private UUID specificationId;

    @Indexed
    private UUID versionId;

    private String prompt;

    private String title;

    private String description;

    private List<String> userStories;

    private List<String> acceptanceCriteria;

    private List<String> functionalRequirements;

    private List<String> nonFunctionalRequirements;

    private String aiModel;

    private String generatedBy;

    private String promptVersion;

    private LocalDateTime generatedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
