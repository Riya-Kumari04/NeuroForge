package com.neuroforge.backend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BurndownResponse {

    private UUID sprintId;
    private String sprintName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalStoryPoints;
    private List<BurndownPointResponse> points;
}
