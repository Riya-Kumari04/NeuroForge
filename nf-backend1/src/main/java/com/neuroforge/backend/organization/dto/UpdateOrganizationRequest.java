package com.neuroforge.backend.organization.dto;

import lombok.Data;

@Data
public class UpdateOrganizationRequest {
    private String name;
    private String industry;
    private String size;
    private String description;
}
