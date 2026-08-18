package com.neuroforge.backend.integration.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.integration.repository.TaskCommitLinkRepository;
import com.neuroforge.backend.integration.entity.TaskCommitLink;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.integration.dto.ConnectRepositoryRequest;
import com.neuroforge.backend.integration.dto.RepositoryConnectionResponse;
import com.neuroforge.backend.integration.entity.RepositoryConnection;
import com.neuroforge.backend.integration.repository.RepositoryConnectionRepository;
import lombok.RequiredArgsConstructor;
import com.neuroforge.backend.integration.entity.CommitCache;

import com.neuroforge.backend.integration.repository.CommitCacheRepository;

import com.neuroforge.backend.integration.dto.RepositorySyncResponse;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.time.OffsetDateTime;
import com.neuroforge.backend.integration.dto.TaskCommitResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RepositoryConnectionServiceImpl implements RepositoryConnectionService {

        private final RepositoryConnectionRepository repositoryConnectionRepository;
        private final RestTemplate restTemplate;
        private final CommitCacheRepository commitCacheRepository;
        private final TaskCommitLinkRepository taskCommitLinkRepository;

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

                String repoUrl = repository.getRepositoryUrl();

                String path = repoUrl
                                .replace("https://github.com/", "")
                                .replace(".git", "");

                String apiUrl = "https://api.github.com/repos/" + path + "/commits";

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(repository.getAccessToken());
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));

                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<List> githubResponse = restTemplate.exchange(
                                apiUrl,
                                HttpMethod.GET,
                                entity,
                                List.class);

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> commits = (List<Map<String, Object>>) githubResponse.getBody();
                repository.setLastSyncTime(LocalDateTime.now());
                repositoryConnectionRepository.save(repository);

                RepositorySyncResponse response = RepositorySyncResponse.builder()
                                .id(repository.getId())
                                .repositoryUrl(repository.getRepositoryUrl())
                                .lastSyncedAt(repository.getLastSyncTime())
                                .message("Repository synced successfully")
                                .build();

                if (commits != null) {

                        for (Map<String, Object> commit : commits) {

                                String sha = (String) commit.get("sha");

                                if (commitCacheRepository.existsByCommitSha(sha)) {
                                        continue;
                                }

                                Map<String, Object> commitInfo = (Map<String, Object>) commit.get("commit");

                                Map<String, Object> author = (Map<String, Object>) commitInfo.get("author");

                                CommitCache commitCache = CommitCache.builder()
                                                .commitSha(sha)
                                                .authorName((String) author.get("name"))
                                                .commitMessage((String) commitInfo.get("message"))
                                                .commitUrl((String) commit.get("html_url"))
                                                .branchName(repository.getDefaultBranch())
                                                .committedAt(
                                                                OffsetDateTime.parse((String) author.get("date"))
                                                                                .toLocalDateTime())
                                                .repositoryConnection(repository)
                                                .build();

                                commitCache = commitCacheRepository.save(commitCache);

                                String message = commitCache.getCommitMessage();

                                Pattern pattern = Pattern.compile("NF-\\d+");
                                Matcher matcher = pattern.matcher(message);

                                while (matcher.find()) {

                                        TaskCommitLink link = TaskCommitLink.builder()
                                                        .taskKey(matcher.group())
                                                        .commit(commitCache)
                                                        .build();

                                        taskCommitLinkRepository.save(link);
                                }
                        }
                }

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

        @Override
        @Transactional(readOnly = true)
        public ApiResponse<List<TaskCommitResponse>> getTaskCommits(String taskKey) {

                List<TaskCommitResponse> commits = taskCommitLinkRepository.findByTaskKey(taskKey)
                                .stream()
                                .map(link -> {
                                        CommitCache commit = link.getCommit();

                                        return TaskCommitResponse.builder()
                                                        .commitSha(commit.getCommitSha())
                                                        .commitMessage(commit.getCommitMessage())
                                                        .authorName(commit.getAuthorName())
                                                        .commitUrl(commit.getCommitUrl())
                                                        .branchName(commit.getBranchName())
                                                        .committedAt(commit.getCommittedAt())
                                                        .build();
                                })
                                .toList();

                return ApiResponse.ok(
                                "Task commits fetched successfully",
                                commits);
        }
}