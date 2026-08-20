package com.neuroforge.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.neuroforge.backend.ai.enums.IssueSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
