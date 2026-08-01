package com.neuroforge.backend.bug.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCheckResponse {

    private boolean duplicate;
    private Long duplicateBugId;
    private String message;
}