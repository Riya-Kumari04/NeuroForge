package com.neuroforge.backend.analytics.entity;

import com.neuroforge.backend.analytics.enums.DeploymentEnvironment;
import com.neuroforge.backend.analytics.enums.DeploymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deployment_records", indexes = {
    @Index(name = "idx_environment", columnList = "environment"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_deployed_at", columnList = "deployed_at"),
    @Index(name = "idx_env_status_time", columnList = "environment, status, deployed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeploymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "deployment_identifier", nullable = false)
    private String deploymentIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false)
    private DeploymentEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeploymentStatus status;

    @Column(name = "deployed_at", nullable = false)
    private LocalDateTime deployedAt;
}
