package com.neuroforge.backend.dto;

import com.neuroforge.backend.entity.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    private UUID id;
    private UUID organizationId;
    private UUID teamId;
    private String email;
    private String invitationToken;
    private InvitationStatus status;
    private LocalDateTime expiresAt;
    private UUID invitedBy;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
