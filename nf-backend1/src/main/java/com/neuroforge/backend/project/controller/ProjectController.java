package com.neuroforge.backend.project.controller;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.project.dto.CreateProjectRequest;
import com.neuroforge.backend.project.dto.ProjectDto;
import com.neuroforge.backend.project.dto.ProjectStatsDto;
import com.neuroforge.backend.project.dto.UpdateProjectRequest;
import com.neuroforge.backend.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Get all projects")
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get projects by organization")
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getProjectsByOrganization(
            @PathVariable Long organizationId) {
        return ResponseEntity.ok(projectService.getProjectsByOrganization(organizationId));
    }

    @PostMapping
    @Operation(summary = "Create Project")
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {

        return ResponseEntity.ok(projectService.createProject(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Project")
    public ResponseEntity<ApiResponse<ProjectDto>> getProject(@PathVariable Long id) {

        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Project")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {

        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Project")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {

        return ResponseEntity.ok(projectService.deleteProject(id));
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get Project Stats / Health")
    public ResponseEntity<ApiResponse<ProjectStatsDto>> getProjectStats(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectStats(id));
    }
}