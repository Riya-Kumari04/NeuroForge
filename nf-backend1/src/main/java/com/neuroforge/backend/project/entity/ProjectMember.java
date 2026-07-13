package com.neuroforge.backend.project.entity;

import com.neuroforge.backend.organization.entity.TeamMember;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Existing Team Member
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id", nullable = false)
    private TeamMember teamMember;

    // Role inside the project
    @Column(nullable = false)
    @Builder.Default
    private String role = "MEMBER";

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @PrePersist
    public void onCreate() {
        assignedAt = LocalDateTime.now();
    }
}