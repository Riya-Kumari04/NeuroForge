package com.neuroforge.backend.ai.dto;

import com.neuroforge.backend.ai.enums.ReviewSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzeReviewRequest {

    @NotNull
    private Long taskId;

    @NotNull
    private Long requestedBy;

    @NotNull
    private ReviewSource reviewSource;

    @NotBlank
    private String language;

    @NotBlank
    private String sourceCode;

    private String commitSha;
}
