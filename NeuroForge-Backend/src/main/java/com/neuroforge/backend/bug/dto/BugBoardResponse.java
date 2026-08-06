package com.neuroforge.backend.bug.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BugBoardResponse {

    private List<BugResponse> open;
    private List<BugResponse> triaged;
    private List<BugResponse> inProgress;
    private List<BugResponse> fixed;
    private List<BugResponse> verified;
    private List<BugResponse> closed;
}