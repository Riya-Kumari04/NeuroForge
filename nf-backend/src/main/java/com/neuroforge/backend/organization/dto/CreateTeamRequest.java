package com.neuroforge.backend.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTeamRequest {
    @NotBlank(message = "Team name is required")
    @Size(min = 2, max = 100)
    private String name;

    private String description;
    private Long leadId;
}
