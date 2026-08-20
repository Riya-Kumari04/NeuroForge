package com.neuroforge.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyzeReviewResponse {

    private Long reviewId;
    private Integer overallScore;
    private String summary;
    private List<ReviewIssueResponse> issues;
}
