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
public class IssueTrendPointResponse {

    private LocalDate date;
    private Integer totalIssues;
    private Integer highIssues;
    private Integer mediumIssues;
    private Integer lowIssues;
    private Integer infoIssues;
}
