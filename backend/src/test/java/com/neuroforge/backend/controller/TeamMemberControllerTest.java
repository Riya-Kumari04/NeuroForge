package com.neuroforge.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroforge.backend.dto.CreateTeamMemberRequest;
import com.neuroforge.backend.dto.TeamMemberResponse;
import com.neuroforge.backend.entity.TeamMemberRole;
import com.neuroforge.backend.entity.TeamMemberStatus;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.service.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TeamMemberController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
public class TeamMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamMemberService teamMemberService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID teamId;
    private UUID memberId;
    private UUID userId;
    private CreateTeamMemberRequest request;
    private TeamMemberResponse response;

    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        userId = UUID.randomUUID();

        request = CreateTeamMemberRequest.builder()
                .userId(userId)
                .role(TeamMemberRole.MEMBER)
                .status(TeamMemberStatus.INVITED)
                .build();

        response = TeamMemberResponse.builder()
                .id(memberId)
                .teamId(teamId)
                .userId(userId)
                .role(TeamMemberRole.MEMBER)
                .status(TeamMemberStatus.INVITED)
                .build();
    }

    @Test
    void createMember_Success() throws Exception {
        when(teamMemberService.createMember(eq(teamId), any(CreateTeamMemberRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/teams/{teamId}/members", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(memberId.toString()))
                .andExpect(jsonPath("$.teamId").value(teamId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.role").value("MEMBER"));

        verify(teamMemberService).createMember(eq(teamId), any(CreateTeamMemberRequest.class));
    }

    @Test
    void createMember_InvalidRequest_Returns400() throws Exception {
        CreateTeamMemberRequest invalidReq = CreateTeamMemberRequest.builder()
                .userId(null) // invalid
                .build();

        mockMvc.perform(post("/api/teams/{teamId}/members", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.userId").exists());

        verify(teamMemberService, never()).createMember(any(UUID.class), any(CreateTeamMemberRequest.class));
    }

    @Test
    void getAllMembers_Success() throws Exception {
        when(teamMemberService.getAllMembers(teamId)).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/teams/{teamId}/members", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(memberId.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));

        verify(teamMemberService).getAllMembers(teamId);
    }

    @Test
    void getMemberById_Success() throws Exception {
        when(teamMemberService.getMemberById(teamId, memberId)).thenReturn(response);

        mockMvc.perform(get("/api/teams/{teamId}/members/{memberId}", teamId, memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(memberId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()));

        verify(teamMemberService).getMemberById(teamId, memberId);
    }

    @Test
    void getMemberById_NotFound_Returns404() throws Exception {
        when(teamMemberService.getMemberById(teamId, memberId)).thenThrow(new ResourceNotFoundException("Member not found"));

        mockMvc.perform(get("/api/teams/{teamId}/members/{memberId}", teamId, memberId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Member not found"));

        verify(teamMemberService).getMemberById(teamId, memberId);
    }

    @Test
    void updateMember_Success() throws Exception {
        when(teamMemberService.updateMember(eq(teamId), eq(memberId), any(CreateTeamMemberRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/teams/{teamId}/members/{memberId}", teamId, memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(memberId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()));

        verify(teamMemberService).updateMember(eq(teamId), eq(memberId), any(CreateTeamMemberRequest.class));
    }

    @Test
    void deleteMember_Success() throws Exception {
        doNothing().when(teamMemberService).deleteMember(teamId, memberId);

        mockMvc.perform(delete("/api/teams/{teamId}/members/{memberId}", teamId, memberId))
                .andExpect(status().isNoContent());

        verify(teamMemberService).deleteMember(teamId, memberId);
    }
}
