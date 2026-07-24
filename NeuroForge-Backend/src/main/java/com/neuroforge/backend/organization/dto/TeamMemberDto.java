package com.neuroforge.backend.organization.dto;

import com.neuroforge.backend.organization.entity.TeamMember;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TeamMemberDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long teamId;
    private Long organizationId;
    private String role;
    private LocalDateTime joinedAt;

    public static TeamMemberDto from(TeamMember m) {
        return TeamMemberDto.builder()
                .id(m.getId())
                .userId(m.getUser() != null ? m.getUser().getId() : null)
                .userName(m.getUser() != null ? m.getUser().getName() : null)
                .userEmail(m.getUser() != null ? m.getUser().getEmail() : null)
                .teamId(m.getTeam() != null ? m.getTeam().getId() : null)
                .organizationId(m.getOrganization() != null ? m.getOrganization().getId() : null)
                .role(m.getRole() != null ? m.getRole().name() : null)
                .joinedAt(m.getJoinedAt())
                .build();
    }
}
