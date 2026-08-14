package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.*;
import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Current user profile ─────────────────────────────────────────────────

    @GetMapping("/me")
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<ApiResponse<UserDTO>> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved", UserDTO.from(currentUser)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user's profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateProfileRequest req) {
        if (req.getName() != null && !req.getName().isBlank())
            currentUser.setName(req.getName());
        if (req.getPhone() != null)
            currentUser.setPhone(req.getPhone());
        if (req.getAvatarUrl() != null)
            currentUser.setAvatarUrl(req.getAvatarUrl());
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            boolean exists = userRepository.findByUsername(req.getUsername())
                    .filter(u -> !u.getId().equals(currentUser.getId())).isPresent();
            if (exists) throw AppException.conflict("Username already taken");
            currentUser.setUsername(req.getUsername());
        }
        User saved = userRepository.save(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", UserDTO.from(saved)));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change current user's password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @RequestBody ChangePasswordRequest req) {
        if (!passwordEncoder.matches(req.getCurrentPassword(), currentUser.getPassword()))
            throw AppException.badRequest("Current password is incorrect");
        if (req.getNewPassword() == null || req.getNewPassword().length() < 8)
            throw AppException.badRequest("New password must be at least 8 characters");
        currentUser.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully"));
    }

    @GetMapping("/me/preferences")
    @Operation(summary = "Get current user's preferences")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPreferences(
            @AuthenticationPrincipal User currentUser) {
        // Use Boolean.TRUE.equals() for null-safe comparison since
        // notificationsEnabled is a Boolean wrapper that may be null in old rows.
        boolean notificationsEnabled = !Boolean.FALSE.equals(currentUser.getNotificationsEnabled());
        Map<String, Object> prefs = Map.of(
                "theme",         currentUser.getTheme()    != null ? currentUser.getTheme()    : "dark",
                "notifications", notificationsEnabled,
                "language",      currentUser.getLanguage() != null ? currentUser.getLanguage() : "English",
                "timezone",      currentUser.getTimezone() != null ? currentUser.getTimezone() : "UTC"
        );
        return ResponseEntity.ok(ApiResponse.ok("Preferences retrieved", prefs));
    }

    @PutMapping("/me/preferences")
    @Operation(summary = "Save current user's preferences")
    public ResponseEntity<ApiResponse<Void>> savePreferences(
            @AuthenticationPrincipal User currentUser,
            @RequestBody PreferencesRequest req) {
        if (req.getTheme() != null)         currentUser.setTheme(req.getTheme());
        if (req.getLanguage() != null)      currentUser.setLanguage(req.getLanguage());
        if (req.getTimezone() != null)      currentUser.setTimezone(req.getTimezone());
        if (req.getNotifications() != null) currentUser.setNotificationsEnabled(req.getNotifications());
        userRepository.save(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Preferences saved"));
    }

    // ── Admin user management ─────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Create a new user (Super Admin only)")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest req) {
        // Check if username already exists
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw AppException.conflict("Username already taken");
        }
        // Check if email already exists
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw AppException.conflict("Email already taken");
        }

        User user = User.builder()
                .name(req.getName())
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .organizationId(req.getOrganizationId())
                .enabled(req.getEnabled() != null ? req.getEnabled() : true)
                .build();

        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("User created successfully", UserDTO.from(saved)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN')")
    @Operation(summary = "List all users (Super Admin / Org Admin only)")
    public ResponseEntity<ApiResponse<List<UserDTO>>> listUsers() {
        List<UserDTO> users = userRepository.findAll()
                .stream().map(UserDTO::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved", users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(ApiResponse.ok("User found", UserDTO.from(u))))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Delete a user (Super Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted"));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN')")
    @Operation(summary = "Approve or reject a user (Super Admin / Org Admin only)")
    public ResponseEntity<ApiResponse<UserDTO>> approveUser(
            @PathVariable Long id,
            @Valid @RequestBody ApproveUserRequest req,
            @AuthenticationPrincipal User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("APPROVE".equals(req.getAction())) {
            user.setApprovalStatus("APPROVED");
            user.setApprovedBy(currentUser.getId());
            user.setApprovedAt(java.time.LocalDateTime.now());
        } else if ("REJECT".equals(req.getAction())) {
            user.setApprovalStatus("REJECTED");
            user.setApprovedBy(currentUser.getId());
            user.setApprovedAt(java.time.LocalDateTime.now());
        } else {
            throw new RuntimeException("Invalid action. Must be APPROVE or REJECT");
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("User approval status updated", UserDTO.from(saved)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN')")
    @Operation(summary = "Get pending users awaiting approval")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getPendingUsers() {
        List<UserDTO> users = userRepository.findAll()
                .stream()
                .filter(u -> "PENDING".equals(u.getApprovalStatus()))
                .map(UserDTO::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Pending users retrieved", users));
    }
}
