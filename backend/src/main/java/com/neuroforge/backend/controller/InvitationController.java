package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.CreateInvitationRequest;
import com.neuroforge.backend.dto.InvitationResponse;
import com.neuroforge.backend.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/api/teams/{teamId}/invitations")
    public ResponseEntity<InvitationResponse> createInvitation(
            @PathVariable UUID teamId,
            @Valid @RequestBody CreateInvitationRequest request) {
        InvitationResponse response = invitationService.createInvitation(teamId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/teams/{teamId}/invitations")
    public ResponseEntity<List<InvitationResponse>> getAllInvitations(@PathVariable UUID teamId) {
        List<InvitationResponse> response = invitationService.getAllInvitations(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/teams/{teamId}/invitations/{id}")
    public ResponseEntity<InvitationResponse> getInvitationById(
            @PathVariable UUID teamId,
            @PathVariable UUID id) {
        InvitationResponse response = invitationService.getInvitationById(teamId, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/teams/{teamId}/invitations/{id}")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable UUID teamId,
            @PathVariable UUID id) {
        invitationService.deleteInvitation(teamId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/invitations/accept/{token}")
    public ResponseEntity<InvitationResponse> acceptInvitation(@PathVariable String token) {
        InvitationResponse response = invitationService.acceptInvitation(token);
        return ResponseEntity.ok(response);
    }
}
