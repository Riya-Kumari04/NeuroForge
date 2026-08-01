package com.neuroforge.backend.dto;

import com.neuroforge.backend.enums.CodeReviewStatus;
import com.neuroforge.backend.enums.ReviewSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReviewResponse {
    private UUID id;
    private UUID taskId;
    private Long requestedBy;
    private Long approvedBy;
    private CodeReviewStatus status;
    private ReviewSource reviewSource;
    private Integer overallScore;
    private String summary;
    private String sourceReference;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
