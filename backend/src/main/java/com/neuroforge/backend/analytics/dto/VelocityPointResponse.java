package com.neuroforge.backend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VelocityPointResponse {

    private UUID sprintId;
    private String sprintName;
    private Integer completedStoryPoints;
    private Long completedTasks;
    private LocalDate sprintEndDate;
}
