package com.neuroforge.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTaskRequest {

    @NotBlank
    private String title;

    private String description;

    private String priority;

    private String status;

    @NotNull
    private Long projectId;

    private Long sprintId;

    private Long assignedToId;

    private LocalDateTime dueDate;

}