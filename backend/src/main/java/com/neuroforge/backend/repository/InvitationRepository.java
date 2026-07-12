package com.neuroforge.backend.repository;

import com.neuroforge.backend.entity.Invitation;
import com.neuroforge.backend.entity.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    Optional<Invitation> findByInvitationToken(String invitationToken);
    List<Invitation> findByTeamId(UUID teamId);
    boolean existsByTeamIdAndEmailAndStatus(UUID teamId, String email, InvitationStatus status);
}
