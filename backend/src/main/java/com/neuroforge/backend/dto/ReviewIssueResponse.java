package com.neuroforge.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.neuroforge.backend.enums.IssueSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewIssueResponse {

    private Integer line;
    private IssueSeverity severity;
    private String category;
    private String description;
    private String suggestion;
}
