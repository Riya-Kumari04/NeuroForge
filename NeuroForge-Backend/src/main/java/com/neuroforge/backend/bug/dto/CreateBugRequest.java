package com.neuroforge.backend.bug.dto;

import lombok.Data;

@Data
public class CreateBugRequest {

    private String title;

    private String description;

    private String severity;

    private String environment;

    private String attachmentUrl;
}