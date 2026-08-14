package com.neuroforge.backend.integration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ConnectRepositoryRequest {

    @NotBlank(message = "Repository name is required")
    private String repositoryName;

    @NotBlank(message = "Owner is required")
    private String owner;

    @NotBlank(message = "Repository URL is required")
    @Pattern(regexp = "^https://github\\.com/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+(\\.git)?$", message = "Repository URL must be a valid GitHub repository URL (e.g., https://github.com/owner/repo)")
    private String repositoryUrl;

    @NotBlank(message = "GitHub token is required")
    private String githubToken;

    @jakarta.validation.constraints.NotNull(message = "Project ID is required")
    private Long projectId;
}
