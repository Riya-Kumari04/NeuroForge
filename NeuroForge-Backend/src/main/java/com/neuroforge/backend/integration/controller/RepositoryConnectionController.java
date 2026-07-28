package com.neuroforge.backend.integration.controller;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.integration.dto.RepositorySyncResponse;
import com.neuroforge.backend.integration.dto.ConnectRepositoryRequest;
import com.neuroforge.backend.integration.dto.RepositoryConnectionResponse;
import com.neuroforge.backend.integration.service.RepositoryConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.neuroforge.backend.integration.dto.TaskCommitResponse;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
public class RepositoryConnectionController {

    private final RepositoryConnectionService repositoryConnectionService;

    @PostMapping("/connect")
    public ApiResponse<RepositoryConnectionResponse> connectRepository(
            @Valid @RequestBody ConnectRepositoryRequest request) {

        return repositoryConnectionService.connectRepository(request);
    }

    @PostMapping("/{repositoryId}/sync")
    public ResponseEntity<ApiResponse<RepositorySyncResponse>> syncRepository(
            @PathVariable Long repositoryId) {

        return ResponseEntity.ok(
                repositoryConnectionService.syncRepository(repositoryId));
    }

    @GetMapping("/tasks/{taskKey}/commits")
    public ApiResponse<List<TaskCommitResponse>> getTaskCommits(
            @PathVariable String taskKey) {

        return repositoryConnectionService.getTaskCommits(taskKey);
    }
}