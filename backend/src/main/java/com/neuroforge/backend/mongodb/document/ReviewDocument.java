package com.neuroforge.backend.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "review_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDocument {

    @Id
    private String id;

    private UUID reviewId;

    private UUID taskId;

    private String model;

    private String language;

    private String sourceCode;

    private Integer overallScore;

    private String summary;

    private List<ReviewIssue> issues;

    private LocalDateTime createdAt;
}
