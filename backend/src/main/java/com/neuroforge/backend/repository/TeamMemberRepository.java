package com.neuroforge.backend.repository;

import com.neuroforge.backend.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeamId(UUID teamId);
    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);
    boolean existsByTeamIdAndUserIdAndIdNot(UUID teamId, UUID userId, UUID id);

    Optional<TeamMember> findByUserEmail(String email);
}
