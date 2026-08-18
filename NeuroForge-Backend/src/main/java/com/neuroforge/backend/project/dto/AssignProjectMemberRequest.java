package com.neuroforge.backend.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignProjectMemberRequest {

    @NotNull
    private Long projectId;

    @NotNull
    private Long teamMemberId;
}