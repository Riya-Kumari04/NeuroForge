package com.neuroforge.backend.project.repository;

import com.neuroforge.backend.project.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProjectId(Long projectId);

}