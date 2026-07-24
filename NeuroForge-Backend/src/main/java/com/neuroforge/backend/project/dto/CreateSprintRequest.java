package com.neuroforge.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateSprintRequest {

    @NotBlank
    private String sprintName;

    private String goal;

    private String status;

    @NotNull
    private Long projectId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

}