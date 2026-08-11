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
public class SprintHealthSummaryResponse {

    private UUID sprintId;
    private String sprintName;
    private LocalDate generatedAt;
    private String overallHealth;
    private String summary;
    private List<String> risks;
    private List<String> recommendations;
}
