package com.neuroforge.backend.organization.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrgStatsDto {
    private long teamsCount;
    private long membersCount;
    private long pendingInvitesCount;
    private long projectsCount;
}
