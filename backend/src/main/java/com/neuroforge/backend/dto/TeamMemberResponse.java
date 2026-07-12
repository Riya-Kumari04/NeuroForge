package com.neuroforge.backend.dto;

import com.neuroforge.backend.entity.TeamMemberRole;
import com.neuroforge.backend.entity.TeamMemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {
    private UUID id;
    private UUID teamId;
    private UUID userId;
    private TeamMemberRole role;
    private TeamMemberStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
