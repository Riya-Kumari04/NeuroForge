package com.neuroforge.backend.project.controller;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.project.dto.AssignProjectMemberRequest;
import com.neuroforge.backend.project.dto.ProjectMemberDto;
import com.neuroforge.backend.project.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-members")
@RequiredArgsConstructor
@Tag(name = "Project Members")
@SecurityRequirement(name = "bearerAuth")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    // ── Write operations: Project Manager, Org Admin, Super Admin ────────────

    @PostMapping
    @Operation(summary = "Assign member to project")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN','ROLE_PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectMemberDto>> assignMember(
            @Valid @RequestBody AssignProjectMemberRequest request) {
        return ResponseEntity.ok(projectMemberService.assignMember(request));
    }

    @DeleteMapping("/{projectMemberId}")
    @Operation(summary = "Remove member from project")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN','ROLE_PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long projectMemberId) {
        return ResponseEntity.ok(projectMemberService.removeMember(projectMemberId));
    }

    // ── Read operations: all authenticated users ──────────────────────────────
    // Developers and Testers need to read project members (e.g. to see task assignments).

    @GetMapping("/{projectId}")
    @Operation(summary = "Get all members of a project")
    public ResponseEntity<ApiResponse<List<ProjectMemberDto>>> getMembers(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
    }
}
