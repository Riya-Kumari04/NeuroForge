package com.neuroforge.backend.integration.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "repository_connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_name", nullable = false)
    private String repositoryName;

    @Column(nullable = false)
    private String owner;

    @Column(name = "repository_url", nullable = false)
    private String repositoryUrl;

    @Column(name = "default_branch", nullable = false)
    @Builder.Default
    private String defaultBranch = "main";

    @Column(name = "access_token", columnDefinition = "TEXT", nullable = false)
    private String accessToken;

    @Column(nullable = false)
    @Builder.Default
    private Boolean connected = false;

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}