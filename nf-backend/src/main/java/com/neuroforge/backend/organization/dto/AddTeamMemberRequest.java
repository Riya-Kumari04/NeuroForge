package com.neuroforge.backend.organization.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddTeamMemberRequest {
    @NotNull
    private Long memberId; // TeamMember id (org member id)
}
