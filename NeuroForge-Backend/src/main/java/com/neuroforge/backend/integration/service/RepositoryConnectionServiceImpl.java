package com.neuroforge.backend.integration.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.integration.dto.ConnectRepositoryRequest;
import com.neuroforge.backend.integration.dto.RepositoryConnectionResponse;
import com.neuroforge.backend.integration.entity.RepositoryConnection;
import com.neuroforge.backend.integration.repository.RepositoryConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RepositoryConnectionServiceImpl implements RepositoryConnectionService {

    private final RepositoryConnectionRepository repositoryConnectionRepository;

    @Override
    public ApiResponse<RepositoryConnectionResponse> connectRepository(
            ConnectRepositoryRequest request) {

        if (repositoryConnectionRepository.existsByRepositoryUrl(request.getRepositoryUrl())) {
            throw AppException.badRequest("Repository is already connected");
        }

        RepositoryConnection repository = RepositoryConnection.builder()
                .repositoryUrl(request.getRepositoryUrl())
                .accessToken(request.getGithubToken())
                .defaultBranch("main")
                .connected(true)
                .build();

        repository = repositoryConnectionRepository.save(repository);

        RepositoryConnectionResponse response = RepositoryConnectionResponse.builder()
                .id(repository.getId())
                .repositoryUrl(repository.getRepositoryUrl())
                .branchName(repository.getDefaultBranch())
                .lastSyncedAt(repository.getLastSyncTime())
                .active(repository.getConnected())
                .build();

        return ApiResponse.ok(
                "Repository connected successfully",
                response);
    }
}