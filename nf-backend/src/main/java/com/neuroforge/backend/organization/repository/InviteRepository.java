package com.neuroforge.backend.organization.repository;

import com.neuroforge.backend.organization.entity.Invite;
import com.neuroforge.backend.organization.entity.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {
    List<Invite> findByOrganizationId(Long organizationId);
    List<Invite> findByOrganizationIdAndStatus(Long organizationId, InviteStatus status);
    Optional<Invite> findByToken(String token);
    long countByOrganizationIdAndStatus(Long organizationId, InviteStatus status);
}
