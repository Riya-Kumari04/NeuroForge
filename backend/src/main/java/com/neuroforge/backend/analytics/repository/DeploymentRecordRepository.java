package com.neuroforge.backend.analytics.repository;

import com.neuroforge.backend.analytics.entity.DeploymentRecord;
import com.neuroforge.backend.analytics.enums.DeploymentEnvironment;
import com.neuroforge.backend.analytics.enums.DeploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeploymentRecordRepository extends JpaRepository<DeploymentRecord, UUID> {

    List<DeploymentRecord> findByEnvironmentAndDeployedAtBetweenOrderByDeployedAtAsc(
            DeploymentEnvironment environment,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByEnvironmentAndStatusAndDeployedAtBetween(
            DeploymentEnvironment environment,
            DeploymentStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<DeploymentRecord> findByEnvironmentAndStatusAndDeployedAtBetween(
            DeploymentEnvironment environment,
            DeploymentStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<DeploymentRecord> findByEnvironmentAndDeployedAtBetween(
            DeploymentEnvironment environment,
            LocalDateTime start,
            LocalDateTime end
    );
}
