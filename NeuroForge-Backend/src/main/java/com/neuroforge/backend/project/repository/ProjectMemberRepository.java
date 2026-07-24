package com.neuroforge.backend.project.repository;

import com.neuroforge.backend.project.entity.Project;
import com.neuroforge.backend.project.entity.ProjectMember;
import com.neuroforge.backend.organization.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProject(Project project);

    /**
     * Eagerly fetch all lazy associations (teamMember → user, teamMember → team)
     * in a single JOIN FETCH query so that ProjectMemberDto.from() can safely
     * access them outside a Hibernate session.
     */
    @Query("SELECT pm FROM ProjectMember pm " +
           "JOIN FETCH pm.teamMember tm " +
           "JOIN FETCH tm.user " +
           "LEFT JOIN FETCH tm.team " +
           "WHERE pm.project = :project")
    List<ProjectMember> findByProjectWithDetails(@Param("project") Project project);

    boolean existsByProjectAndTeamMember(Project project, TeamMember teamMember);

    long countByProjectId(Long projectId);

    /**
     * Bulk-delete all ProjectMember records for a given TeamMember.
     * Called before removing a member from the organisation so the FK constraint
     * to team_members is satisfied.
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ProjectMember pm WHERE pm.teamMember.id = :teamMemberId")
    void deleteByTeamMemberId(@Param("teamMemberId") Long teamMemberId);

    /**
     * Bulk-delete all ProjectMember records for every project that belongs to the
     * given organisation.  Called before deleting an organisation so the FK
     * constraint from project_members → team_members is removed first.
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ProjectMember pm WHERE pm.project.organization.id = :orgId")
    void deleteByProjectOrganizationId(@Param("orgId") Long orgId);
}
