package com.neuroforge.backend.integration.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskCommitResponse {

    private String commitSha;
    private String commitMessage;
    private String authorName;
    private String commitUrl;
    private String branchName;
    private LocalDateTime committedAt;
}