package com.neuroforge.backend.bug.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BugResponse {

    private Long id;

    private String title;

    private String description;

    private String severity;

    private String status;

    private String environment;

    private String attachmentUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}