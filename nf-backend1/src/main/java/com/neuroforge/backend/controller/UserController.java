package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.dto.UserDTO;
import com.neuroforge.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;

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
}
