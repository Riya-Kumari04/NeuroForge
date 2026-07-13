package com.neuroforge.backend.project.controller;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.project.dto.CreateSprintRequest;
import com.neuroforge.backend.project.dto.SprintDto;
import com.neuroforge.backend.project.dto.UpdateSprintRequest;
import com.neuroforge.backend.project.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprint Management")
@SecurityRequirement(name = "bearerAuth")
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    @Operation(summary = "Create Sprint")
    public ResponseEntity<ApiResponse<SprintDto>> createSprint(
            @Valid @RequestBody CreateSprintRequest request) {

        return ResponseEntity.ok(sprintService.createSprint(request));
    }

    @GetMapping
    @Operation(summary = "Get All Sprints")
    public ResponseEntity<ApiResponse<List<SprintDto>>> getAllSprints() {

        return ResponseEntity.ok(sprintService.getAllSprints());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Sprint By Id")
    public ResponseEntity<ApiResponse<SprintDto>> getSprintById(
            @PathVariable Long id) {

        return ResponseEntity.ok(sprintService.getSprintById(id));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get Sprints By Project")
    public ResponseEntity<ApiResponse<List<SprintDto>>> getProjectSprints(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(sprintService.getProjectSprints(projectId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Sprint")
    public ResponseEntity<ApiResponse<SprintDto>> updateSprint(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSprintRequest request) {

        return ResponseEntity.ok(sprintService.updateSprint(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Sprint")
    public ResponseEntity<ApiResponse<Void>> deleteSprint(
            @PathVariable Long id) {

        return ResponseEntity.ok(sprintService.deleteSprint(id));
    }
}