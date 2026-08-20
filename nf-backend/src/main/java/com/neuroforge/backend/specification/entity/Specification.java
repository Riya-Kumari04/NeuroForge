package com.neuroforge.backend.specification.entity;

import com.neuroforge.backend.specification.enums.SpecificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "specifications",
        indexes = {
                @Index(name = "idx_spec_key", columnList = "specification_key"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_deleted", columnList = "deleted")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Specification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "specification_key",
            nullable = false,
            unique = true,
            length = 30
    )
    private String specificationKey;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "current_version", nullable = false)
    private Integer currentVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SpecificationStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @OneToMany(
            mappedBy = "specification",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<SpecificationVersion> versions = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addVersion(SpecificationVersion version) {
        versions.add(version);
        version.setSpecification(this);
    }
}
