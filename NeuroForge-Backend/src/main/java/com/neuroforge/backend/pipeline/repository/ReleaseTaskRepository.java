package com.neuroforge.backend.pipeline.repository;

import com.neuroforge.backend.pipeline.entity.ReleaseTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReleaseTaskRepository extends JpaRepository<ReleaseTask, Long> {

    List<ReleaseTask> findByReleaseId(Long releaseId);
}