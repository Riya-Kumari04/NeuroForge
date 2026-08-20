package com.neuroforge.backend.dto;

import com.neuroforge.backend.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {
    private Long          id;
    private String        username;
    private String        email;
    private String        name;
    private String        phone;
    private String        avatarUrl;
    private String        role;
    private Long          organizationId;
    private boolean       enabled;
    private LocalDateTime createdAt;
    private String        approvalStatus;
    private Long          approvedBy;
    private LocalDateTime approvedAt;

    public static UserDTO from(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .organizationId(user.getOrganizationId())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .approvalStatus(user.getApprovalStatus())
                .approvedBy(user.getApprovedBy())
                .approvedAt(user.getApprovedAt())
                .build();
    }
}
