package com.neuroforge.backend.project.repository;

import com.neuroforge.backend.project.entity.Project;
import com.neuroforge.backend.project.entity.ProjectMember;
import com.neuroforge.backend.organization.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProject(Project project);

    boolean existsByProjectAndTeamMember(Project project, TeamMember teamMember);

}