package com.neuroforge.backend.ai.dto;

import com.neuroforge.backend.ai.enums.CodeReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCodeReviewStatusRequest {

    @NotNull
    private CodeReviewStatus status;

    private Integer overallScore;

    private String summary;
}
