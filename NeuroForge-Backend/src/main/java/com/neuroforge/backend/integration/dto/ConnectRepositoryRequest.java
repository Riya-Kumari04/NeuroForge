package com.neuroforge.backend.integration.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConnectRepositoryRequest {

    @NotBlank(message = "Repository URL is required")
    private String repositoryUrl;

    @NotBlank(message = "GitHub token is required")
    private String githubToken;
}