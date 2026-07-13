package com.neuroforge.backend.organization.dto;

import com.neuroforge.backend.organization.entity.Organization;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrganizationDto {
    private Long id;
    private String name;
    private String slug;
    private String industry;
    private String size;
    private String plan;
    private String description;
    private String logoUrl;
    private long teamsCount;
    private long membersCount;
    private LocalDateTime createdAt;

    public static OrganizationDto from(Organization org) {
        return OrganizationDto.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .industry(org.getIndustry())
                .size(org.getSize())
                .plan(org.getPlan())
                .description(org.getDescription())
                .logoUrl(org.getLogoUrl())
                .teamsCount(org.getTeams() != null ? org.getTeams().size() : 0)
                .membersCount(org.getTeamMembers() != null ? org.getTeamMembers().size() : 0)
                .createdAt(org.getCreatedAt())
                .build();
    }
}
