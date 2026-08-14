package com.neuroforge.backend.integration.repository;

import com.neuroforge.backend.integration.entity.CommitCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitCacheRepository extends JpaRepository<CommitCache, Long> {

    boolean existsByCommitSha(String commitSha);

}
