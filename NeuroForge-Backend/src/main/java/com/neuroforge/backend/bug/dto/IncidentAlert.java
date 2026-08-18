package com.neuroforge.backend.bug.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IncidentAlert {

    private Long incidentId;
    private Long bugId;
    private String title;
    private String severity;
    private String status;
}