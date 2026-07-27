package com.neuroforge.backend.integration.service;

import java.util.List;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.integration.dto.ConnectRepositoryRequest;
import com.neuroforge.backend.integration.dto.RepositoryConnectionResponse;

public interface RepositoryConnectionService {

    ApiResponse<RepositoryConnectionResponse> connectRepository(
            ConnectRepositoryRequest request);

    ApiResponse<List<RepositoryConnectionResponse>> getAllRepositories();

}