package com.neuroforge.backend.project.dto;

import com.neuroforge.backend.project.entity.Project;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioProjectDto {

    private Long id;
    private String projectName;
    private String description;
    private String status;
    private String health;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static PortfolioProjectDto from(Project project) {

        String health = "ON_TRACK";

        if ("COMPLETED".equalsIgnoreCase(project.getStatus())) {
            health = "COMPLETED";
        } else if (project.getEndDate() != null &&
                project.getEndDate().isBefore(LocalDateTime.now())) {
            health = "DELAYED";
        }

        return PortfolioProjectDto.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .status(project.getStatus())
                .health(health)
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .build();
    }
}