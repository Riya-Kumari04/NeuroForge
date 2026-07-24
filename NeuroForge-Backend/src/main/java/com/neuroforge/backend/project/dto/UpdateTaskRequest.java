package com.neuroforge.backend.project.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTaskRequest {

    private String title;

    private String description;

    private String priority;

    private String status;

    private Long sprintId;

    private Long assignedToId;

    private LocalDateTime dueDate;

}