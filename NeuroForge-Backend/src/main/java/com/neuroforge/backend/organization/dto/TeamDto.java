package com.neuroforge.backend.organization.dto;

import com.neuroforge.backend.organization.entity.Team;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TeamDto {
    private Long id;
    private String name;
    private String description;
    private Long organizationId;
    private Long leadId;
    private String leadName;
    private long membersCount;
    private LocalDateTime createdAt;

    public static TeamDto from(Team team) {
        return TeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .organizationId(team.getOrganization() != null ? team.getOrganization().getId() : null)
                .leadId(team.getLead() != null ? team.getLead().getId() : null)
                .leadName(team.getLead() != null ? team.getLead().getName() : null)
                .membersCount(team.getMembers() != null ? team.getMembers().size() : 0)
                .createdAt(team.getCreatedAt())
                .build();
    }
}
