package com.neuroforge.backend.project.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateProjectRequest {

    private String projectName;

    private String description;

    private String status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

}