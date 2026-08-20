package com.neuroforge.backend.ai.dto;

import com.neuroforge.backend.ai.enums.CodeReviewStatus;
import com.neuroforge.backend.ai.enums.ReviewSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeReviewResponse {

    private Long id;
    private Long taskId;
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
