package com.neuroforge.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroforge.backend.dto.CreateOrganizationRequest;
import com.neuroforge.backend.dto.OrganizationResponse;
import com.neuroforge.backend.entity.OrganizationStatus;
import com.neuroforge.backend.exception.DuplicateResourceException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.service.OrganizationService;
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

@WebMvcTest(controllers = OrganizationController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
public class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganizationService organizationService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateOrganizationRequest request;
    private OrganizationResponse response;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        request = CreateOrganizationRequest.builder()
                .name("Neuro Forge")
                .slug("neuro-forge")
                .description("SDLC platform")
                .status(OrganizationStatus.ACTIVE)
                .build();

        response = OrganizationResponse.builder()
                .id(orgId)
                .name("Neuro Forge")
                .slug("neuro-forge")
                .description("SDLC platform")
                .status(OrganizationStatus.ACTIVE)
                .build();
    }

    @Test
    void createOrganization_Success() throws Exception {
        when(organizationService.createOrganization(any(CreateOrganizationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orgId.toString()))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.slug").value(request.getSlug()));

        verify(organizationService).createOrganization(any(CreateOrganizationRequest.class));
    }

    @Test
    void createOrganization_InvalidRequest_Returns400() throws Exception {
        CreateOrganizationRequest invalidReq = CreateOrganizationRequest.builder()
                .name("") // blank
                .slug("Invalid Slug") // contains uppercase and spaces
                .build();

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.slug").exists());

        verify(organizationService, never()).createOrganization(any(CreateOrganizationRequest.class));
    }

    @Test
    void getAllOrganizations_Success() throws Exception {
        when(organizationService.getAllOrganizations()).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orgId.toString()))
                .andExpect(jsonPath("$[0].name").value(response.getName()));

        verify(organizationService).getAllOrganizations();
    }

    @Test
    void getOrganizationById_Success() throws Exception {
        when(organizationService.getOrganizationById(orgId)).thenReturn(response);

        mockMvc.perform(get("/api/organizations/{id}", orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orgId.toString()))
                .andExpect(jsonPath("$.name").value(response.getName()));

        verify(organizationService).getOrganizationById(orgId);
    }

    @Test
    void getOrganizationById_NotFound_Returns404() throws Exception {
        when(organizationService.getOrganizationById(orgId)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/organizations/{id}", orgId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found"));

        verify(organizationService).getOrganizationById(orgId);
    }

    @Test
    void updateOrganization_Success() throws Exception {
        when(organizationService.updateOrganization(eq(orgId), any(CreateOrganizationRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/organizations/{id}", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orgId.toString()))
                .andExpect(jsonPath("$.name").value(response.getName()));

        verify(organizationService).updateOrganization(eq(orgId), any(CreateOrganizationRequest.class));
    }

    @Test
    void deleteOrganization_Success() throws Exception {
        doNothing().when(organizationService).deleteOrganization(orgId);

        mockMvc.perform(delete("/api/organizations/{id}", orgId))
                .andExpect(status().isNoContent());

        verify(organizationService).deleteOrganization(orgId);
    }
}
