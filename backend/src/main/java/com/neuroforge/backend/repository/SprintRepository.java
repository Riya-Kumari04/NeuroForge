package com.neuroforge.backend.repository;

import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    Optional<Sprint> findFirstByStatus(SprintStatus status);

    boolean existsByTeamIdAndStatus(UUID teamId, SprintStatus status);

    boolean existsByStatus(SprintStatus status);

    List<Sprint> findAllByOrderByStartDateAsc();
}
