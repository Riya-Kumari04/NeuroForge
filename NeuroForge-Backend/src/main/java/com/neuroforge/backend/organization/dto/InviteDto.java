package com.neuroforge.backend.organization.dto;

import com.neuroforge.backend.organization.entity.Invite;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InviteDto {
    private Long id;
    private String email;
    private Long organizationId;
    private String orgName;
    private String status;
    private String role;
    private String invitedByName;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime respondedAt;

    public static InviteDto from(Invite invite) {
        return InviteDto.builder()
                .id(invite.getId())
                .email(invite.getEmail())
                .organizationId(invite.getOrganization() != null ? invite.getOrganization().getId() : null)
                .orgName(invite.getOrganization() != null ? invite.getOrganization().getName() : null)
                .status(invite.getStatus() != null ? invite.getStatus().name() : null)
                .role(invite.getRole() != null ? invite.getRole().name() : null)
                .invitedByName(invite.getInvitedBy() != null ? invite.getInvitedBy().getName() : null)
                .createdAt(invite.getCreatedAt())
                .expiresAt(invite.getExpiresAt())
                .respondedAt(invite.getRespondedAt())
                .build();
    }
}
