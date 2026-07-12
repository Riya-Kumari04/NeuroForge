package com.neuroforge.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroforge.backend.dto.CreateTeamRequest;
import com.neuroforge.backend.dto.TeamResponse;
import com.neuroforge.backend.entity.TeamStatus;
import com.neuroforge.backend.exception.DuplicateResourceException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.service.TeamService;
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

@WebMvcTest(controllers = TeamController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
public class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID orgId;
    private UUID teamId;
    private CreateTeamRequest request;
    private TeamResponse response;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        teamId = UUID.randomUUID();

        request = CreateTeamRequest.builder()
                .name("Backend Team")
                .description("Handles backend APIs")
                .status(TeamStatus.ACTIVE)
                .build();

        response = TeamResponse.builder()
                .id(teamId)
                .organizationId(orgId)
                .name("Backend Team")
                .description("Handles backend APIs")
                .status(TeamStatus.ACTIVE)
                .build();
    }

    @Test
    void createTeam_Success() throws Exception {
        when(teamService.createTeam(eq(orgId), any(CreateTeamRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/organizations/{orgId}/teams", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.organizationId").value(orgId.toString()))
                .andExpect(jsonPath("$.name").value(request.getName()));

        verify(teamService).createTeam(eq(orgId), any(CreateTeamRequest.class));
    }

    @Test
    void createTeam_InvalidName_Returns400() throws Exception {
        CreateTeamRequest invalidReq = CreateTeamRequest.builder()
                .name("") // blank name
                .build();

        mockMvc.perform(post("/api/organizations/{orgId}/teams", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        verify(teamService, never()).createTeam(any(UUID.class), any(CreateTeamRequest.class));
    }

    @Test
    void getAllTeams_Success() throws Exception {
        when(teamService.getAllTeams(orgId)).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/organizations/{orgId}/teams", orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(teamId.toString()))
                .andExpect(jsonPath("$[0].name").value(response.getName()));

        verify(teamService).getAllTeams(orgId);
    }

    @Test
    void getTeamById_Success() throws Exception {
        when(teamService.getTeamById(orgId, teamId)).thenReturn(response);

        mockMvc.perform(get("/api/organizations/{orgId}/teams/{teamId}", orgId, teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.name").value(response.getName()));

        verify(teamService).getTeamById(orgId, teamId);
    }

    @Test
    void getTeamById_NotFound_Returns404() throws Exception {
        when(teamService.getTeamById(orgId, teamId)).thenThrow(new ResourceNotFoundException("Team not found"));

        mockMvc.perform(get("/api/organizations/{orgId}/teams/{teamId}", orgId, teamId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Team not found"));

        verify(teamService).getTeamById(orgId, teamId);
    }

    @Test
    void updateTeam_Success() throws Exception {
        when(teamService.updateTeam(eq(orgId), eq(teamId), any(CreateTeamRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/organizations/{orgId}/teams/{teamId}", orgId, teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.name").value(response.getName()));

        verify(teamService).updateTeam(eq(orgId), eq(teamId), any(CreateTeamRequest.class));
    }

    @Test
    void deleteTeam_Success() throws Exception {
        doNothing().when(teamService).deleteTeam(orgId, teamId);

        mockMvc.perform(delete("/api/organizations/{orgId}/teams/{teamId}", orgId, teamId))
                .andExpect(status().isNoContent());

        verify(teamService).deleteTeam(orgId, teamId);
    }
}
