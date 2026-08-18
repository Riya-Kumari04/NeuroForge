package com.neuroforge.backend.bug.dto;


import lombok.Data;

@Data
public class UpdateBugStatusRequest {

    private String status;

    private String role;

}