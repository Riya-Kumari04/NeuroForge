package com.neuroforge.backend.organization.dto;

import com.neuroforge.backend.organization.entity.OrgRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteMemberRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    @NotNull(message = "Role is required")
    private OrgRole role;
}
