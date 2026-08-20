package com.neuroforge.backend.pipeline.repository;

import com.neuroforge.backend.pipeline.entity.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineRepository extends JpaRepository<Pipeline, Long> {
}
