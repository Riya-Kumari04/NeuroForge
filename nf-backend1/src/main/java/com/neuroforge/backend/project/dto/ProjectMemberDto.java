package com.neuroforge.backend.project.dto;

import com.neuroforge.backend.project.entity.ProjectMember;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectMemberDto {

    private Long id;

    private Long projectId;

    private Long teamMemberId;

    private String memberName;

    private String role;

    public static ProjectMemberDto from(ProjectMember member) {

        return ProjectMemberDto.builder()
                .id(member.getId())
                .projectId(member.getProject().getId())
                .teamMemberId(member.getTeamMember().getId())
                .memberName(member.getTeamMember().getUser().getName())
                .role(member.getRole())
                .build();
    }
}