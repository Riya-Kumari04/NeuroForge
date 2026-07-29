package com.neuroforge.backend.pipeline.repository;

import com.neuroforge.backend.pipeline.entity.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {

    List<PipelineRun> findByPipelineId(Long pipelineId);

}