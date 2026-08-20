package com.neuroforge.backend.project.dto;

import com.neuroforge.backend.project.entity.ProjectMember;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectMemberDto {

    private Long id;
    private Long projectId;
    private Long teamMemberId;
    private String memberName;
    private String memberEmail;
    private String role;
    private Long teamId;
    private String teamName;
    private LocalDateTime assignedAt;

    public static ProjectMemberDto from(ProjectMember member) {
        return ProjectMemberDto.builder()
                .id(member.getId())
                .projectId(member.getProject().getId())
                .teamMemberId(member.getTeamMember().getId())
                .memberName(member.getTeamMember().getUser() != null ? member.getTeamMember().getUser().getName() : null)
                .memberEmail(member.getTeamMember().getUser() != null ? member.getTeamMember().getUser().getEmail() : null)
                .role(member.getRole())
                .teamId(member.getTeamMember().getTeam() != null ? member.getTeamMember().getTeam().getId() : null)
                .teamName(member.getTeamMember().getTeam() != null ? member.getTeamMember().getTeam().getName() : null)
                .assignedAt(member.getAssignedAt())
                .build();
    }
}
