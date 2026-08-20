package com.neuroforge.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApproveUserRequest {

    @NotBlank(message = "Action is required")
    private String action; // APPROVE or REJECT

    private String rejectionReason;
}
