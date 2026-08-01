package com.neuroforge.backend.dto;

import com.neuroforge.backend.enums.ReviewSource;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCodeReviewRequest {

    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @NotNull(message = "Requested by user ID is required")
    private Long requestedBy;

    @NotNull(message = "Review source is required")
    private ReviewSource reviewSource;

    private String sourceReference;
}
