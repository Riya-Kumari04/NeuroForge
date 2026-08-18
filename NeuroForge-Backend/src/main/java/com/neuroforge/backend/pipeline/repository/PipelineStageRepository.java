package com.neuroforge.backend.pipeline.repository;

import com.neuroforge.backend.pipeline.entity.PipelineRun;
import com.neuroforge.backend.pipeline.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {

    List<PipelineStage> findByPipelineRun(PipelineRun pipelineRun);
}