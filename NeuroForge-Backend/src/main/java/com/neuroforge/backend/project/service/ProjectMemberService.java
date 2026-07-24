package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.project.dto.AssignProjectMemberRequest;
import com.neuroforge.backend.project.dto.ProjectMemberDto;

import java.util.List;

public interface ProjectMemberService {

    ApiResponse<ProjectMemberDto> assignMember(AssignProjectMemberRequest request);

    ApiResponse<List<ProjectMemberDto>> getProjectMembers(Long projectId);

    ApiResponse<Void> removeMember(Long projectMemberId);

}