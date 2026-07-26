package com.neuroforge.backend.integration.controller;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.integration.dto.ConnectRepositoryRequest;
import com.neuroforge.backend.integration.dto.RepositoryConnectionResponse;
import com.neuroforge.backend.integration.service.RepositoryConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}