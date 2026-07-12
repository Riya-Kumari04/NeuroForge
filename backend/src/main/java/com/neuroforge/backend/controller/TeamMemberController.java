package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.CreateTeamMemberRequest;
import com.neuroforge.backend.dto.TeamMemberResponse;
import com.neuroforge.backend.service.TeamMemberService;
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
@RequestMapping("/api/teams/{teamId}/members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @PostMapping
    public ResponseEntity<TeamMemberResponse> createMember(
            @PathVariable UUID teamId,
            @Valid @RequestBody CreateTeamMemberRequest request) {
        TeamMemberResponse response = teamMemberService.createMember(teamId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TeamMemberResponse>> getAllMembers(@PathVariable UUID teamId) {
        List<TeamMemberResponse> response = teamMemberService.getAllMembers(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<TeamMemberResponse> getMemberById(
            @PathVariable UUID teamId,
            @PathVariable UUID memberId) {
        TeamMemberResponse response = teamMemberService.getMemberById(teamId, memberId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<TeamMemberResponse> updateMember(
            @PathVariable UUID teamId,
            @PathVariable UUID memberId,
            @Valid @RequestBody CreateTeamMemberRequest request) {
        TeamMemberResponse response = teamMemberService.updateMember(teamId, memberId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable UUID teamId,
            @PathVariable UUID memberId) {
        teamMemberService.deleteMember(teamId, memberId);
        return ResponseEntity.noContent().build();
    }
}
