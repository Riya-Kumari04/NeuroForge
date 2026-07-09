package com.springboard.auth_service.repository;

import com.springboard.auth_service.entity.Otp;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findByEmail(String email);
    @Modifying
    @Transactional
    void deleteAllByEmail(String email);
    Optional<Otp> findTopByEmailOrderByExpiryTimeDesc(String email);}
