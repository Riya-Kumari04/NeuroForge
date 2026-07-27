package com.neuroforge.backend.integration.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.integration.dto.ConnectRepositoryRequest;
import com.neuroforge.backend.integration.dto.RepositoryConnectionResponse;
import com.neuroforge.backend.integration.entity.RepositoryConnection;
import com.neuroforge.backend.integration.repository.RepositoryConnectionRepository;
import lombok.RequiredArgsConstructor;
import com.neuroforge.backend.integration.dto.RepositorySyncResponse;

import java.time.LocalDateTime;
import java.util.List;
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
                                .repositoryName(request.getRepositoryName())
                                .owner(request.getOwner())
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

        @Override
        public ApiResponse<RepositorySyncResponse> syncRepository(Long repositoryId) {

                RepositoryConnection repository = repositoryConnectionRepository
                                .findById(repositoryId)
                                .orElseThrow(() -> AppException.notFound("Repository not found"));

                repository.setLastSyncTime(LocalDateTime.now());

                repository = repositoryConnectionRepository.save(repository);

                RepositorySyncResponse response = RepositorySyncResponse.builder()
                                .id(repository.getId())
                                .repositoryUrl(repository.getRepositoryUrl())
                                .lastSyncedAt(repository.getLastSyncTime())
                                .message("Repository synced successfully")
                                .build();

                return ApiResponse.ok(
                                "Repository synced successfully",
                                response);
        }

        @Override
        @Transactional(readOnly = true)
        public ApiResponse<List<RepositoryConnectionResponse>> getAllRepositories() {

                List<RepositoryConnectionResponse> repositories = repositoryConnectionRepository.findAll()
                                .stream()
                                .map(repository -> RepositoryConnectionResponse.builder()
                                                .id(repository.getId())
                                                .repositoryUrl(repository.getRepositoryUrl())
                                                .branchName(repository.getDefaultBranch())
                                                .lastSyncedAt(repository.getLastSyncTime())
                                                .active(repository.getConnected())
                                                .build())
                                .toList();

                return ApiResponse.ok(
                                "Repositories fetched successfully",
                                repositories);
        }
}