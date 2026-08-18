package com.neuroforge.backend.organization.entity;

import com.neuroforge.backend.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrgRole role = OrgRole.DEVELOPER;

    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    public void onCreate() { joinedAt = LocalDateTime.now(); }
}
