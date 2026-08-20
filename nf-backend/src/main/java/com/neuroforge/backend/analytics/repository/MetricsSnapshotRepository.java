package com.neuroforge.backend.analytics.repository;

import com.neuroforge.backend.analytics.entity.MetricsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetricsSnapshotRepository extends JpaRepository<MetricsSnapshot, UUID> {

    Optional<MetricsSnapshot> findBySnapshotDate(LocalDate snapshotDate);

    List<MetricsSnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsBySnapshotDate(LocalDate snapshotDate);
}
