package com.neuroforge.backend.dto;

import com.neuroforge.backend.enums.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSprintRequest {

    @NotBlank(message = "Sprint name is required")
    private String name;

    private String goal;

    private LocalDate startDate;

    private LocalDate endDate;

    private SprintStatus status;

    private UUID teamId;
}
