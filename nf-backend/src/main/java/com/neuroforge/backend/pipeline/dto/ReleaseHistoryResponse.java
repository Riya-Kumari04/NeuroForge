package com.neuroforge.backend.pipeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseHistoryResponse {

    private Long id;
    private String version;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime releasedAt;
    private String releaseNotes;
}
