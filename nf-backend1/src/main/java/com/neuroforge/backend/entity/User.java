package com.neuroforge.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email",    columnList = "email",    unique = true),
    @Index(name = "idx_user_username", columnList = "username", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    /**
     * One of: ROLE_SUPER_ADMIN | ROLE_ORG_ADMIN | ROLE_PROJECT_MANAGER
     *         ROLE_DEVELOPER   | ROLE_TESTER    | ROLE_CLIENT
     */
    @Column(nullable = false)
    @Builder.Default
    private String role = "ROLE_DEVELOPER";

    @Column(name = "organization_id")
    private Long organizationId;

    /** false until the user verifies the OTP sent at registration */
    @Builder.Default
    private boolean enabled = false;

    @Column(name = "account_non_expired")  @Builder.Default private boolean accountNonExpired     = true;
    @Column(name = "account_non_locked")   @Builder.Default private boolean accountNonLocked       = true;
    @Column(name = "credentials_non_expired") @Builder.Default private boolean credentialsNonExpired = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist  protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate   protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // ── UserDetails ──────────────────────────────────────────────────────────
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }
    @Override public boolean isAccountNonExpired()     { return accountNonExpired; }
    @Override public boolean isAccountNonLocked()      { return accountNonLocked; }
    @Override public boolean isCredentialsNonExpired() { return credentialsNonExpired; }
    @Override public boolean isEnabled()               { return enabled; }
}
