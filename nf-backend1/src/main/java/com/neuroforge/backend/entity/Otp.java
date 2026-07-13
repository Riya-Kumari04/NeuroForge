package com.neuroforge.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otps", indexes = {
    @Index(name = "idx_otp_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String otp;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Builder.Default
    private boolean used = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
