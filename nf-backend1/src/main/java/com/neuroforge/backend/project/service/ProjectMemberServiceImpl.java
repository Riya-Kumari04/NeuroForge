package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.organization.entity.TeamMember;
import com.neuroforge.backend.organization.repository.TeamMemberRepository;
import com.neuroforge.backend.project.dto.AssignProjectMemberRequest;
import com.neuroforge.backend.project.dto.ProjectMemberDto;
import com.neuroforge.backend.project.entity.Project;
import com.neuroforge.backend.project.entity.ProjectMember;
import com.neuroforge.backend.project.entity.Task;
import com.neuroforge.backend.project.repository.ProjectMemberRepository;
import com.neuroforge.backend.project.repository.ProjectRepository;
import com.neuroforge.backend.project.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public ApiResponse<ProjectMemberDto> assignMember(AssignProjectMemberRequest request) {

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> AppException.notFound("Project not found"));

        TeamMember teamMember = teamMemberRepository.findById(request.getTeamMemberId())
                .orElseThrow(() -> AppException.notFound("Team member not found"));

        if (projectMemberRepository.existsByProjectAndTeamMember(project, teamMember)) {
            throw AppException.badRequest("Member already assigned to this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .teamMember(teamMember)
                .role("MEMBER")
                .build();

        member = projectMemberRepository.save(member);

        return ApiResponse.ok(
                "Team member assigned successfully",
                ProjectMemberDto.from(member)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ProjectMemberDto>> getProjectMembers(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> AppException.notFound("Project not found"));

        List<ProjectMemberDto> members = projectMemberRepository.findByProject(project)
                .stream()
                .map(ProjectMemberDto::from)
                .toList();

        return ApiResponse.ok("Project members fetched successfully", members);
    }

    @Override
    @Transactional
    public ApiResponse<Void> removeMember(Long projectMemberId) {

        ProjectMember member = projectMemberRepository.findById(projectMemberId)
                .orElseThrow(() -> AppException.notFound("Project member not found"));

        // Unassign any tasks currently delegated to this member before removing
        // them from the project, so the FK on tasks.project_member_id never blocks the delete.
        List<Task> assignedTasks = taskRepository.findByAssignedToId(projectMemberId);
        if (!assignedTasks.isEmpty()) {
            assignedTasks.forEach(task -> task.setAssignedTo(null));
            taskRepository.saveAll(assignedTasks);
        }

        projectMemberRepository.delete(member);

        return ApiResponse.ok("Member removed successfully");
    }
}