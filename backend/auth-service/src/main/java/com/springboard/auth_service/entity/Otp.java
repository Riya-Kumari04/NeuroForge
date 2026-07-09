package com.springboard.auth_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String otp;

    private String email;

    private LocalDateTime expiryTime;

    private boolean used;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}