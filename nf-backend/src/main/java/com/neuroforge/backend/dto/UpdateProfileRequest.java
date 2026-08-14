package com.neuroforge.backend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String username;
    private String phone;
    private String avatarUrl;
}
