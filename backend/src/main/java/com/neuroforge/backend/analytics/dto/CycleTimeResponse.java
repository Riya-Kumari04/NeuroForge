package com.neuroforge.backend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CycleTimeResponse {

    private Double averageCycleTimeHours;
    private Long completedTasks;
    private Long measuredTasks;
    private List<CycleTimePointResponse> points;
}
