package com.neuroforge.backend.bug.repository;

import com.neuroforge.backend.bug.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByBugId(Long bugId);

}