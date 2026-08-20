package com.neuroforge.backend.integration.repository;

import com.neuroforge.backend.integration.entity.TaskCommitLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCommitLinkRepository extends JpaRepository<TaskCommitLink, Long> {

    List<TaskCommitLink> findByTaskKey(String taskKey);

    List<TaskCommitLink> findByTaskId(Long taskId);

    java.util.Optional<TaskCommitLink> findByCommitIdAndTaskId(Long commitId, Long taskId);

}
