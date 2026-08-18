package com.neuroforge.backend.pipeline.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReleaseNoteResponse {

    private String version;

    private String releaseNotes;
}