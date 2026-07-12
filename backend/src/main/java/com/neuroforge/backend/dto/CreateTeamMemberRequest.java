package com.neuroforge.backend.dto;

import com.neuroforge.backend.entity.TeamMemberRole;
import com.neuroforge.backend.entity.TeamMemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamMemberRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private TeamMemberRole role;

    private TeamMemberStatus status;
}
