package com.neuroforge.backend.integration.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RepositoryConnectionResponse {

    private Long id;

    private String repositoryUrl;

    private String branchName;

    private LocalDateTime lastSyncedAt;

    private boolean active;
}