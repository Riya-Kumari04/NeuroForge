package com.neuroforge.backend.project.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateSprintRequest {

    private String sprintName;

    private String goal;

    private String status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

}