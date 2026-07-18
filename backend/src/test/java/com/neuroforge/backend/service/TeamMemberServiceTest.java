package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CreateTeamMemberRequest;
import com.neuroforge.backend.dto.TeamMemberResponse;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.entity.TeamMember;
import com.neuroforge.backend.entity.TeamMemberRole;
import com.neuroforge.backend.entity.TeamMemberStatus;
import com.neuroforge.backend.exception.DuplicateResourceException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.exception.AccessDeniedException;
import com.neuroforge.backend.repository.TeamMemberRepository;
import com.neuroforge.backend.repository.TeamRepository;
import com.neuroforge.backend.security.TeamMemberAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TeamMemberServiceTest {

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberAccessValidator teamMemberAccessValidator;

    @InjectMocks
    private TeamMemberService teamMemberService;

    private UUID teamId;
    private UUID memberId;
    private UUID userId;
    private Team team;
    private CreateTeamMemberRequest request;
    private TeamMember teamMember;

    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        userId = UUID.randomUUID();

        team = Team.builder()
                .id(teamId)
                .name("Core Platform")
                .build();

        request = CreateTeamMemberRequest.builder()
                .userId(userId)
                .role(TeamMemberRole.TEAM_LEAD)
                .status(TeamMemberStatus.ACTIVE)
                .build();

        teamMember = TeamMember.builder()
                .id(memberId)
                .team(team)
                .userId(userId)
                .role(TeamMemberRole.TEAM_LEAD)
                .status(TeamMemberStatus.ACTIVE)
                .build();
    }

    @Test
    void createMember_Success() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)).thenReturn(false);
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);

        TeamMemberResponse response = teamMemberService.createMember(teamId, request);

        assertNotNull(response);
        assertEquals(memberId, response.getId());
        assertEquals(teamId, response.getTeamId());
        assertEquals(userId, response.getUserId());
        assertEquals(TeamMemberRole.TEAM_LEAD, response.getRole());

        verify(teamRepository).findById(teamId);
        verify(teamMemberRepository).existsByTeamIdAndUserId(teamId, userId);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    void createMember_TeamNotFound_ThrowsException() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamMemberService.createMember(teamId, request));

        verify(teamRepository).findById(teamId);
        verify(teamMemberRepository, never()).existsByTeamIdAndUserId(any(UUID.class), any(UUID.class));
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }

    @Test
    void createMember_DuplicateUser_ThrowsException() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> teamMemberService.createMember(teamId, request));

        verify(teamRepository).findById(teamId);
        verify(teamMemberRepository).existsByTeamIdAndUserId(teamId, userId);
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }

    @Test
    void createMember_DefaultRoleAndStatus() {
        CreateTeamMemberRequest defaultReq = CreateTeamMemberRequest.builder()
                .userId(userId)
                .build();

        TeamMember defaultMember = TeamMember.builder()
                .id(memberId)
                .team(team)
                .userId(userId)
                .role(TeamMemberRole.MEMBER)
                .status(TeamMemberStatus.INVITED)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)).thenReturn(false);
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(defaultMember);

        TeamMemberResponse response = teamMemberService.createMember(teamId, defaultReq);

        assertNotNull(response);
        assertEquals(TeamMemberRole.MEMBER, response.getRole());
        assertEquals(TeamMemberStatus.INVITED, response.getStatus());

        verify(teamRepository).findById(teamId);
        verify(teamMemberRepository).existsByTeamIdAndUserId(teamId, userId);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    void getAllMembers_Success() {
        when(teamRepository.existsById(teamId)).thenReturn(true);
        when(teamMemberRepository.findByTeamId(teamId)).thenReturn(Arrays.asList(teamMember));

        List<TeamMemberResponse> list = teamMemberService.getAllMembers(teamId);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(memberId, list.get(0).getId());

        verify(teamRepository).existsById(teamId);
        verify(teamMemberRepository).findByTeamId(teamId);
    }

    @Test
    void getAllMembers_TeamNotFound_ThrowsException() {
        when(teamRepository.existsById(teamId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> teamMemberService.getAllMembers(teamId));

        verify(teamRepository).existsById(teamId);
        verify(teamMemberRepository, never()).findByTeamId(any(UUID.class));
    }

    @Test
    void getMemberById_Success() {
        doNothing().when(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);
        when(teamRepository.existsById(teamId)).thenReturn(true);
        when(teamMemberRepository.findById(memberId)).thenReturn(Optional.of(teamMember));

        TeamMemberResponse response = teamMemberService.getMemberById(teamId, memberId);

        assertNotNull(response);
        assertEquals(memberId, response.getId());

        verify(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);
        verify(teamRepository).existsById(teamId);
        verify(teamMemberRepository).findById(memberId);
    }

    @Test
    void getMemberById_AccessDenied_ThrowsAccessDeniedException() {
        doThrow(new AccessDeniedException("Access denied")).when(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);

        assertThrows(AccessDeniedException.class, () -> teamMemberService.getMemberById(teamId, memberId));

        verify(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);
        verify(teamRepository, never()).existsById(any(UUID.class));
        verify(teamMemberRepository, never()).findById(any(UUID.class));
    }

    @Test
    void getMemberById_NotBelonging_ThrowsException() {
        UUID otherTeamId = UUID.randomUUID();
        doNothing().when(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);
        when(teamRepository.existsById(otherTeamId)).thenReturn(true);
        when(teamMemberRepository.findById(memberId)).thenReturn(Optional.of(teamMember));

        assertThrows(ResourceNotFoundException.class, () -> teamMemberService.getMemberById(otherTeamId, memberId));

        verify(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);
        verify(teamRepository).existsById(otherTeamId);
        verify(teamMemberRepository).findById(memberId);
    }

    @Test
    void updateMember_Success() {
        CreateTeamMemberRequest updateReq = CreateTeamMemberRequest.builder()
                .userId(userId)
                .role(TeamMemberRole.VIEWER)
                .status(TeamMemberStatus.INACTIVE)
                .build();

        TeamMember updatedMember = TeamMember.builder()
                .id(memberId)
                .team(team)
                .userId(userId)
                .role(TeamMemberRole.VIEWER)
                .status(TeamMemberStatus.INACTIVE)
                .build();

        doNothing().when(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);
        when(teamRepository.existsById(teamId)).thenReturn(true);
        when(teamMemberRepository.findById(memberId)).thenReturn(Optional.of(teamMember));
        when(teamMemberRepository.existsByTeamIdAndUserIdAndIdNot(teamId, updateReq.getUserId(), memberId)).thenReturn(false);
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(updatedMember);

        TeamMemberResponse response = teamMemberService.updateMember(teamId, memberId, updateReq);

        assertNotNull(response);
        assertEquals(TeamMemberRole.VIEWER, response.getRole());
        assertEquals(TeamMemberStatus.INACTIVE, response.getStatus());

        verify(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);
        verify(teamRepository).existsById(teamId);
        verify(teamMemberRepository).findById(memberId);
        verify(teamMemberRepository).existsByTeamIdAndUserIdAndIdNot(teamId, updateReq.getUserId(), memberId);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    void updateMember_AccessDenied_ThrowsAccessDeniedException() {
        CreateTeamMemberRequest updateReq = CreateTeamMemberRequest.builder().userId(userId).build();
        doThrow(new AccessDeniedException("Access denied")).when(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);

        assertThrows(AccessDeniedException.class, () -> teamMemberService.updateMember(teamId, memberId, updateReq));

        verify(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);
        verify(teamRepository, never()).existsById(any(UUID.class));
        verify(teamMemberRepository, never()).findById(any(UUID.class));
    }

    @Test
    void deleteMember_Success() {
        doNothing().when(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);
        when(teamRepository.existsById(teamId)).thenReturn(true);
        when(teamMemberRepository.findById(memberId)).thenReturn(Optional.of(teamMember));
        doNothing().when(teamMemberRepository).delete(teamMember);

        teamMemberService.deleteMember(teamId, memberId);

        verify(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);
        verify(teamRepository).existsById(teamId);
        verify(teamMemberRepository).findById(memberId);
        verify(teamMemberRepository).delete(teamMember);
    }

    @Test
    void deleteMember_AccessDenied_ThrowsAccessDeniedException() {
        doThrow(new AccessDeniedException("Access denied")).when(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);

        assertThrows(AccessDeniedException.class, () -> teamMemberService.deleteMember(teamId, memberId));

        verify(teamMemberAccessValidator).verifyTeamMemberAccess(memberId);
        verify(teamRepository, never()).existsById(any(UUID.class));
        verify(teamMemberRepository, never()).findById(any(UUID.class));
    }
}
