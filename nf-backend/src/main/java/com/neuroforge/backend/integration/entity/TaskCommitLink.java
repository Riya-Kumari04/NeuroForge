package com.neuroforge.backend.integration.entity;

import com.neuroforge.backend.project.entity.Task;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_commit_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCommitLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Task ID extracted from commit message (e.g. NF-123)
    @Column(nullable = false)
    private String taskKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commit_id", nullable = false)
    private CommitCache commit;
}
