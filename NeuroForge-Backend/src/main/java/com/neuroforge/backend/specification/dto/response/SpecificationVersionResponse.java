package com.neuroforge.backend.specification.dto.response;

import com.neuroforge.backend.specification.enums.VersionStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationVersionResponse {

    private UUID id;

    private Integer versionNumber;

    private String description;

    private String userStories;

    private String acceptanceCriteria;

    private String functionalRequirements;

    private String nonFunctionalRequirements;

    private VersionStatus status;

    private String generatedBy;

    private LocalDateTime generatedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
