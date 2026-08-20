package com.neuroforge.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateTaskRequest {

    @NotBlank
    private String title;

    private String description;

    private String priority;

    private String status;

    // Module 5: Story Points
    private Integer storyPoints;

    // Module 5: Labels
    private String labels;

    // Module 4: Specification Traceability (optional)
    private UUID specificationId;
    private UUID specificationVersionId;

    @NotNull
    private Long projectId;

    private Long sprintId;

    private Long assignedToId;

    private LocalDateTime dueDate;

}