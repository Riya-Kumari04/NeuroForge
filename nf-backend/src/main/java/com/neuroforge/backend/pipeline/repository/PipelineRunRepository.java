package com.neuroforge.backend.pipeline.repository;

import com.neuroforge.backend.pipeline.entity.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {

    List<PipelineRun> findByPipelineId(Long pipelineId);
    long countByStatus(String status);
}
