package com.neuroforge.backend.dto;

import com.neuroforge.backend.enums.CodeReviewStatus;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCodeReviewStatusRequest {

    @NotNull(message = "Code review status is required")
    private CodeReviewStatus status;

    @Min(value = 0, message = "Overall score must be at least 0")
    @Max(value = 100, message = "Overall score must be at most 100")
    private Integer overallScore;

    @Size(max = 5000, message = "Summary cannot exceed 5000 characters")
    private String summary;
}
