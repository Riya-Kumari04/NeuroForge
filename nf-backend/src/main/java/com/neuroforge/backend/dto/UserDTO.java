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
    private String        role;
    private Long          organizationId;
    private boolean       enabled;
    private LocalDateTime createdAt;

    public static UserDTO from(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .organizationId(user.getOrganizationId())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
