package com.neuroforge.backend.analytics.repository;

import com.neuroforge.backend.analytics.entity.VelocityHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VelocityHistoryRepository extends JpaRepository<VelocityHistory, UUID> {

    List<VelocityHistory> findAllByOrderBySprintEndDateAsc();

    Optional<VelocityHistory> findBySprintId(UUID sprintId);
}
