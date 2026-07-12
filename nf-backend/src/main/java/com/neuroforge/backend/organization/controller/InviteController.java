package com.neuroforge.backend.organization.controller;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitation Actions")
public class InviteController {

    private final OrganizationService orgService;

    @PostMapping("/accept")
    @Operation(summary = "Accept an invitation (no auth required)")
    public ResponseEntity<ApiResponse<Void>> accept(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Token is required"));
        }
        return ResponseEntity.ok(orgService.acceptInvitation(token));
    }

    @PostMapping("/reject")
    @Operation(summary = "Reject an invitation (no auth required)")
    public ResponseEntity<ApiResponse<Void>> reject(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Token is required"));
        }
        return ResponseEntity.ok(orgService.rejectInvitation(token));
    }
}
