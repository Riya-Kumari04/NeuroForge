package com.neuroforge.backend.organization.repository;

import com.neuroforge.backend.organization.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByOrganizationId(Long organizationId);
    List<TeamMember> findByUserId(Long userId);
    Optional<TeamMember> findByUserIdAndOrganizationId(Long userId, Long organizationId);
    long countByOrganizationId(Long organizationId);
}
