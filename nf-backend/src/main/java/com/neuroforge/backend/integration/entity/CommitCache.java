package com.neuroforge.backend.integration.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "commits_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String commitSha;

    @Column(nullable = false)
    private String authorName;

    @Column(columnDefinition = "TEXT")
    private String commitMessage;

    private String commitUrl;

    private String branchName;

    private LocalDateTime committedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private RepositoryConnection repositoryConnection;
}
