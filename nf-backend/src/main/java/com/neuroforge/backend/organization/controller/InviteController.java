package com.neuroforge.backend.organization.controller;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.organization.dto.InviteDto;
import com.neuroforge.backend.organization.entity.Invite;
import com.neuroforge.backend.organization.repository.InviteRepository;
import com.neuroforge.backend.organization.service.OrganizationService;
import com.neuroforge.backend.exception.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitation Actions")
public class InviteController {

    private final OrganizationService orgService;
    private final InviteRepository inviteRepo;

    /** Validate token — no auth required */
    @GetMapping("/validate")
    @Transactional(readOnly = true)
    @Operation(summary = "Validate invitation token and return details")
    public ResponseEntity<ApiResponse<InviteDto>> validate(@RequestParam String token) {
        Invite invite = inviteRepo.findByToken(token)
                .orElseThrow(() -> AppException.notFound("Invalid or expired invitation link"));

        if (invite.getStatus() != com.neuroforge.backend.organization.entity.InviteStatus.PENDING) {
            return ResponseEntity.ok(ApiResponse.fail(
                    "This invitation has already been " + invite.getStatus().name().toLowerCase() + "."));
        }
        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok(ApiResponse.fail("This invitation has expired."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Invitation found", InviteDto.from(invite)));
    }

    @PostMapping("/accept")
    @Operation(summary = "Accept an invitation (no auth required)")
    public ResponseEntity<ApiResponse<Void>> accept(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.fail("Token is required"));
        return ResponseEntity.ok(orgService.acceptInvitation(token));
    }

    @PostMapping("/reject")
    @Operation(summary = "Reject an invitation (no auth required)")
    public ResponseEntity<ApiResponse<Void>> reject(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.fail("Token is required"));
        return ResponseEntity.ok(orgService.rejectInvitation(token));
    }
}
