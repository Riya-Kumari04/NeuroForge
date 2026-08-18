package com.neuroforge.backend.bug.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SLAResponse {

    private Long incidentId;
    private String status;
    private String elapsedTime;
}