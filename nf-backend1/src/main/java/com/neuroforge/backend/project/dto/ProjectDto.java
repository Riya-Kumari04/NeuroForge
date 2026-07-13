package com.neuroforge.backend.project.dto;

import com.neuroforge.backend.project.entity.Project;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

    private Long id;
    private String projectName;
    private String description;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long organizationId;

    public static ProjectDto from(Project project) {

        return ProjectDto.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .organizationId(
                        project.getOrganization() != null
                                ? project.getOrganization().getId()
                                : null)
                .build();
    }
}