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

    @Column
    private String phone;

    @Column
    private String avatarUrl;

    /**
     * One of: ROLE_SUPER_ADMIN | ROLE_ORG_ADMIN | ROLE_PROJECT_MANAGER
     *         ROLE_DEVELOPER   | ROLE_TESTER    | ROLE_CLIENT
     */
    @Column(nullable = false)
    @Builder.Default
    private String role = "ROLE_DEVELOPER";

    @Column(name = "organization_id")
    private Long organizationId;

    /**
     * Approval status for users requiring approval
     * PENDING - awaiting approval
     * APPROVED - approved and can access system
     * REJECTED - rejected access
     */
    @Column(name = "approval_status")
    @Builder.Default
    private String approvalStatus = "APPROVED";

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Use Boolean (wrapper) so that existing rows where these columns are NULL
     * can be loaded without a Hibernate PropertyAccessException.
     * isEnabled(), isAccountNonExpired(), etc. fall back to safe defaults.
     */
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "account_non_expired")     @Builder.Default private Boolean accountNonExpired     = true;
    @Column(name = "account_non_locked")      @Builder.Default private Boolean accountNonLocked       = true;
    @Column(name = "credentials_non_expired") @Builder.Default private Boolean credentialsNonExpired  = true;

    // ── Preferences ───────────────────────────────────────────────────────────
    // Boolean wrapper to tolerate NULL in rows created before these columns existed.
    @Column(name = "notifications_enabled") @Builder.Default private Boolean notificationsEnabled = true;
    @Column @Builder.Default private String language = "English";
    @Column @Builder.Default private String timezone = "UTC";
    @Column @Builder.Default private String theme    = "dark";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist  protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate   protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // ── UserDetails ───────────────────────────────────────────────────────────
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    // Null-safe: fall back to the column's intended default when the DB value is NULL.
    @Override public boolean isEnabled()               {
        // User must be enabled (approval status check removed to allow login after OTP verification)
        return Boolean.TRUE.equals(enabled);
    }
    @Override public boolean isAccountNonExpired()     { return !Boolean.FALSE.equals(accountNonExpired); }
    @Override public boolean isAccountNonLocked()      { return !Boolean.FALSE.equals(accountNonLocked); }
    @Override public boolean isCredentialsNonExpired() { return !Boolean.FALSE.equals(credentialsNonExpired); }
}
