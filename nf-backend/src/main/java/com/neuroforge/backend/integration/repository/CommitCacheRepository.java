package com.neuroforge.backend.integration.repository;

import com.neuroforge.backend.integration.entity.CommitCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommitCacheRepository extends JpaRepository<CommitCache, Long> {

    boolean existsByCommitSha(String commitSha);

    List<CommitCache> findByRepositoryConnectionId(Long repositoryConnectionId);

    Optional<CommitCache> findByCommitSha(String commitSha);
}
