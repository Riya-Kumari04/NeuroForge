package com.neuroforge.backend.bug.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaTimerResponse {

    private Long incidentId;

    private String status;

    private long elapsedSeconds;

    private String elapsedTime;
}