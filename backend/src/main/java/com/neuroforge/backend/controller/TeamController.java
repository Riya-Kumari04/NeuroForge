package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.CreateTeamRequest;
import com.neuroforge.backend.dto.TeamResponse;
import com.neuroforge.backend.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations/{organizationId}/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateTeamRequest request) {
        TeamResponse response = teamService.createTeam(organizationId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams(@PathVariable UUID organizationId) {
        List<TeamResponse> response = teamService.getAllTeams(organizationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById(
            @PathVariable UUID organizationId,
            @PathVariable UUID teamId) {
        TeamResponse response = teamService.getTeamById(organizationId, teamId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable UUID organizationId,
            @PathVariable UUID teamId,
            @Valid @RequestBody CreateTeamRequest request) {
        TeamResponse response = teamService.updateTeam(organizationId, teamId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(
            @PathVariable UUID organizationId,
            @PathVariable UUID teamId) {
        teamService.deleteTeam(organizationId, teamId);
        return ResponseEntity.noContent().build();
    }
}
