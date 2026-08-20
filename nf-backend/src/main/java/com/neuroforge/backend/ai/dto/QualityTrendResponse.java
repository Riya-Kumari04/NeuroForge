package com.neuroforge.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityTrendResponse {

    private Long developerId;
    private String developerName;
    private Double averageScore;
    private Integer totalReviews;
    private Integer acceptedReviews;
    private Integer rejectedReviews;
    private List<TrendDataPoint> trendData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendDataPoint {
        private LocalDateTime date;
        private Integer score;
        private String status;
    }
}
