package com.neuroforge.backend.dto;

import lombok.Data;

@Data
public class PreferencesRequest {
    private String theme;
    private Boolean notifications;
    private String language;
    private String timezone;
}
