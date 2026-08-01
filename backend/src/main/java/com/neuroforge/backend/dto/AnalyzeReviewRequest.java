package com.neuroforge.backend.dto;

import com.neuroforge.backend.enums.ReviewSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzeReviewRequest {

    @NotNull
    private UUID taskId;

    @NotNull
    private Long requestedBy;

    @NotNull
    private ReviewSource reviewSource;

    @NotBlank
    private String language;

    @NotBlank
    private String sourceCode;
}
