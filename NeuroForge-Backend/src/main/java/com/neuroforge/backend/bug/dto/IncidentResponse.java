package com.neuroforge.backend.bug.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IncidentResponse {

    private Long id;
    private Long bugId;
    private String bugTitle;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime resolvedAt;
}