package com.neuroforge.backend.pipeline.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReleaseResponse {

    private Long id;

    private String version;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime releasedAt;
}