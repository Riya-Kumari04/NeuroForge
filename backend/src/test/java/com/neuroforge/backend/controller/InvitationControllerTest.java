package com.neuroforge.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroforge.backend.dto.CreateInvitationRequest;
import com.neuroforge.backend.dto.InvitationResponse;
import com.neuroforge.backend.entity.InvitationStatus;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.service.InvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InvitationController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
public class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvitationService invitationService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID teamId;
    private UUID orgId;
    private UUID inviteId;
    private CreateInvitationRequest request;
    private InvitationResponse response;

    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();
        orgId = UUID.randomUUID();
        inviteId = UUID.randomUUID();

        request = CreateInvitationRequest.builder()
                .email("invitee@example.com")
                .invitedBy(UUID.randomUUID())
                .build();

        response = InvitationResponse.builder()
                .id(inviteId)
                .organizationId(orgId)
                .teamId(teamId)
                .email("invitee@example.com")
                .invitationToken("test-token")
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    void createInvitation_Success() throws Exception {
        when(invitationService.createInvitation(eq(teamId), any(CreateInvitationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/teams/{teamId}/invitations", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(inviteId.toString()))
                .andExpect(jsonPath("$.teamId").value(teamId.toString()))
                .andExpect(jsonPath("$.email").value("invitee@example.com"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(invitationService).createInvitation(eq(teamId), any(CreateInvitationRequest.class));
    }

    @Test
    void createInvitation_InvalidEmail_Returns400() throws Exception {
        CreateInvitationRequest invalidReq = CreateInvitationRequest.builder()
                .email("invalid-email") // not formatted as email
                .build();

        mockMvc.perform(post("/api/teams/{teamId}/invitations", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());

        verify(invitationService, never()).createInvitation(any(), any());
    }

    @Test
    void getAllInvitations_Success() throws Exception {
        when(invitationService.getAllInvitations(teamId)).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/teams/{teamId}/invitations", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(inviteId.toString()))
                .andExpect(jsonPath("$[0].email").value("invitee@example.com"));

        verify(invitationService).getAllInvitations(teamId);
    }

    @Test
    void getInvitationById_Success() throws Exception {
        when(invitationService.getInvitationById(teamId, inviteId)).thenReturn(response);

        mockMvc.perform(get("/api/teams/{teamId}/invitations/{id}", teamId, inviteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inviteId.toString()))
                .andExpect(jsonPath("$.email").value("invitee@example.com"));

        verify(invitationService).getInvitationById(teamId, inviteId);
    }

    @Test
    void getInvitationById_NotFound_Returns404() throws Exception {
        when(invitationService.getInvitationById(teamId, inviteId)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/teams/{teamId}/invitations/{id}", teamId, inviteId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found"));

        verify(invitationService).getInvitationById(teamId, inviteId);
    }

    @Test
    void cancelInvitation_Success() throws Exception {
        doNothing().when(invitationService).deleteInvitation(teamId, inviteId);

        mockMvc.perform(delete("/api/teams/{teamId}/invitations/{id}", teamId, inviteId))
                .andExpect(status().isNoContent());

        verify(invitationService).deleteInvitation(teamId, inviteId);
    }

    @Test
    void acceptInvitation_Success() throws Exception {
        when(invitationService.acceptInvitation("test-token")).thenReturn(response);

        mockMvc.perform(post("/api/invitations/accept/{token}", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING")); // our mock setup has status PENDING, which is fine for verification

        verify(invitationService).acceptInvitation("test-token");
    }
}
