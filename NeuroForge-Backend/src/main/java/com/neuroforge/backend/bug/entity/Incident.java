package com.neuroforge.backend.bug.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bug_id", nullable = false)
    private Bug bug;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    public void onCreate() {
        startedAt = LocalDateTime.now();
    }
}