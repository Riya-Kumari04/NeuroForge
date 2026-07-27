package com.springboard7.requirement.entity;

import com.springboard7.requirement.entity.common.BaseEntity;
import com.springboard7.requirement.enums.VersionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "specification_versions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_specification_version",
                        columnNames = {
                                "specification_id",
                                "version_number"
                        }
                )
        },
        indexes = {
                @Index(name = "idx_specification_id", columnList = "specification_id"),
                @Index(name = "idx_version_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "specification_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_specification_version")
    )
    private Specification specification;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Lob
    @Column(nullable = false)
    private String description;

    @Lob
    private String userStories;

    @Lob
    private String acceptanceCriteria;

    @Lob
    private String functionalRequirements;

    @Lob
    private String nonFunctionalRequirements;

    @Lob
    private String aiPrompt;

    @Lob
    private String aiResponse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VersionStatus status;

    private String generatedBy;

    private LocalDateTime generatedAt;

    private String reviewedBy;

    private LocalDateTime reviewedAt;

    private String approvedBy;

    private LocalDateTime approvedAt;

    @Lob
    private String reviewComments;



}