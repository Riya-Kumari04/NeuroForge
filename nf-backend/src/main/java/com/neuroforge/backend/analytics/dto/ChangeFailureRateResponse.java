package com.neuroforge.backend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeFailureRateResponse {

    private Long totalProductionDeploymentAttempts;
    private Long failedProductionDeployments;
    private Double changeFailureRate;
    private Integer periodDays;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
