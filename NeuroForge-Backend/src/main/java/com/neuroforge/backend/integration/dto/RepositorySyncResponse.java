package com.neuroforge.backend.integration.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RepositorySyncResponse {

    private Long id;
    private String repositoryUrl;
    private LocalDateTime lastSyncedAt;
    private String message;
}