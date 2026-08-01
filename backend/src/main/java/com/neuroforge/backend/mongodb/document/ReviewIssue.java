package com.neuroforge.backend.mongodb.document;

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
public class ReviewIssue {

    private Integer line;
    private IssueSeverity severity;
    private String category;
    private String description;
    private String suggestion;
}
