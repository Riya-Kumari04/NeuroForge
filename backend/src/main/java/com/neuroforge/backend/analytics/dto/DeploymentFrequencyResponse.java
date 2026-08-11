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
public class DeploymentFrequencyResponse {

    private Long totalSuccessfulDeployments;
    private Integer periodDays;
    private Double deploymentsPerDay;
    private Double deploymentsPerWeek;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
