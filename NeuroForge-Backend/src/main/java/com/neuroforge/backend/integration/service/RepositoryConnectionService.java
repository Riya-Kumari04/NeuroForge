package com.neuroforge.backend.integration.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.integration.dto.ConnectRepositoryRequest;
import com.neuroforge.backend.integration.dto.RepositoryConnectionResponse;

public interface RepositoryConnectionService {

    ApiResponse<RepositoryConnectionResponse> connectRepository(
            ConnectRepositoryRequest request
    );

}