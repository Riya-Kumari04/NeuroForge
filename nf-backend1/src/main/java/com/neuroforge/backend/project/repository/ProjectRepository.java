package com.neuroforge.backend.project.repository;

import com.neuroforge.backend.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOrganizationId(Long organizationId);

}