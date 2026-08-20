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
public class VelocityPointResponse {

    private Long sprintId;
    private String sprintName;
    private Integer completedStoryPoints;
    private Long completedTasks;
    private LocalDate sprintEndDate;
}
