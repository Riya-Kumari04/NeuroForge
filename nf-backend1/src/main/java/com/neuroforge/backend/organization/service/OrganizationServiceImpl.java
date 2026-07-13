package com.neuroforge.backend.organization.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.organization.dto.*;
import com.neuroforge.backend.organization.entity.*;
import com.neuroforge.backend.organization.repository.*;
import com.neuroforge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository orgRepo;
    private final TeamRepository teamRepo;
    private final TeamMemberRepository memberRepo;
    private final InviteRepository inviteRepo;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;

    // ── Organizations ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<OrganizationDto> createOrganization(CreateOrganizationRequest req, User currentUser) {
        if (orgRepo.existsBySlug(req.getSlug())) {
            throw AppException.conflict("An organization with this slug already exists");
        }
        Organization org = Organization.builder()
                .name(req.getName())
                .slug(req.getSlug())
                .industry(req.getIndustry())
                .size(req.getSize())
                .plan(req.getPlan() != null ? req.getPlan() : "FREE")
                .description(req.getDescription())
                .createdBy(currentUser)
                .build();
        org = orgRepo.save(org);

        // Add creator as ORG_ADMIN member
        TeamMember creator = TeamMember.builder()
                .user(currentUser)
                .organization(org)
                .role(OrgRole.ORG_ADMIN)
                .build();
        memberRepo.save(creator);

        return ApiResponse.ok("Organization created", OrganizationDto.from(org));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OrganizationDto>> getAllOrganizations(User currentUser) {
        List<Organization> orgs;
        boolean isSuperAdmin = "ROLE_SUPER_ADMIN".equals(currentUser.getRole());
        if (isSuperAdmin) {
            orgs = orgRepo.findAll();
        } else {
            // Return only orgs the user is a member of
            List<Long> orgIds = memberRepo.findByUserId(currentUser.getId())
                    .stream().map(m -> m.getOrganization().getId()).collect(Collectors.toList());
            orgs = orgRepo.findAllById(orgIds);
        }
        List<OrganizationDto> dtos = orgs.stream().map(OrganizationDto::from).collect(Collectors.toList());
        return ApiResponse.ok("Organizations retrieved", dtos);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OrganizationDto> getOrganizationById(Long id) {
        Organization org = orgRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Organization not found"));
        return ApiResponse.ok("Organization found", OrganizationDto.from(org));
    }

    @Override
    @Transactional
    public ApiResponse<OrganizationDto> updateOrganization(Long id, UpdateOrganizationRequest req) {
        Organization org = orgRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Organization not found"));
        if (req.getName() != null)        org.setName(req.getName());
        if (req.getIndustry() != null)    org.setIndustry(req.getIndustry());
        if (req.getSize() != null)        org.setSize(req.getSize());
        if (req.getDescription() != null) org.setDescription(req.getDescription());
        org = orgRepo.save(org);
        return ApiResponse.ok("Organization updated", OrganizationDto.from(org));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteOrganization(Long id) {
        if (!orgRepo.existsById(id)) throw AppException.notFound("Organization not found");
        orgRepo.deleteById(id);
        return ApiResponse.ok("Organization deleted");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OrgStatsDto> getOrgStats(Long id) {
        Organization org = orgRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Organization not found"));
        long teams   = teamRepo.findByOrganizationId(id).size();
        long members = memberRepo.countByOrganizationId(id);
        long pending = inviteRepo.countByOrganizationIdAndStatus(id, InviteStatus.PENDING);
        OrgStatsDto stats = OrgStatsDto.builder()
                .teamsCount(teams).membersCount(members)
                .pendingInvitesCount(pending).projectsCount(0)
                .build();
        return ApiResponse.ok("Stats retrieved", stats);
    }

    // ── Teams ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<TeamDto> createTeam(Long orgId, CreateTeamRequest req) {
        Organization org = orgRepo.findById(orgId)
                .orElseThrow(() -> AppException.notFound("Organization not found"));
        User lead = req.getLeadId() != null ? userRepo.findById(req.getLeadId()).orElse(null) : null;
        Team team = Team.builder()
                .name(req.getName())
                .description(req.getDescription())
                .organization(org)
                .lead(lead)
                .build();
        team = teamRepo.save(team);
        return ApiResponse.ok("Team created", TeamDto.from(team));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TeamDto>> getAllTeams(Long orgId) {
        List<TeamDto> teams = teamRepo.findByOrganizationId(orgId)
                .stream().map(TeamDto::from).collect(Collectors.toList());
        return ApiResponse.ok("Teams retrieved", teams);
    }

    @Override
    @Transactional
    public ApiResponse<TeamDto> updateTeam(Long orgId, Long teamId, CreateTeamRequest req) {
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> AppException.notFound("Team not found"));
        if (!team.getOrganization().getId().equals(orgId))
            throw AppException.badRequest("Team does not belong to this organization");
        if (req.getName() != null)        team.setName(req.getName());
        if (req.getDescription() != null) team.setDescription(req.getDescription());
        if (req.getLeadId() != null) {
            User lead = userRepo.findById(req.getLeadId()).orElse(null);
            team.setLead(lead);
        }
        team = teamRepo.save(team);
        return ApiResponse.ok("Team updated", TeamDto.from(team));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteTeam(Long orgId, Long teamId) {
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> AppException.notFound("Team not found"));
        if (!team.getOrganization().getId().equals(orgId))
            throw AppException.badRequest("Team does not belong to this organization");
        teamRepo.deleteById(teamId);
        return ApiResponse.ok("Team deleted");
    }

    // ── Members ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TeamMemberDto>> getAllMembers(Long orgId) {
        List<TeamMemberDto> members = memberRepo.findByOrganizationId(orgId)
                .stream().map(TeamMemberDto::from).collect(Collectors.toList());
        return ApiResponse.ok("Members retrieved", members);
    }

    @Override
    @Transactional
    public ApiResponse<Void> removeMember(Long orgId, Long memberId) {
        TeamMember member = memberRepo.findById(memberId)
                .orElseThrow(() -> AppException.notFound("Member not found"));
        if (!member.getOrganization().getId().equals(orgId))
            throw AppException.badRequest("Member does not belong to this organization");
        memberRepo.deleteById(memberId);
        return ApiResponse.ok("Member removed");
    }

    // ── Invitations ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<InviteDto> inviteMember(Long orgId, InviteMemberRequest req, User currentUser) {
        Organization org = orgRepo.findById(orgId)
                .orElseThrow(() -> AppException.notFound("Organization not found"));
        Invite invite = Invite.builder()
                .email(req.getEmail())
                .organization(org)
                .invitedBy(currentUser)
                .role(req.getRole())
                .status(InviteStatus.PENDING)
                .build();
        invite = inviteRepo.save(invite);
        sendInvitationEmail(invite);
        return ApiResponse.ok("Invitation sent", InviteDto.from(invite));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<InviteDto>> getAllInvitations(Long orgId) {
        List<InviteDto> invites = inviteRepo.findByOrganizationId(orgId)
                .stream().map(InviteDto::from).collect(Collectors.toList());
        return ApiResponse.ok("Invitations retrieved", invites);
    }

    @Override
    @Transactional
    public ApiResponse<Void> cancelInvitation(Long orgId, Long inviteId) {
        Invite invite = inviteRepo.findById(inviteId)
                .orElseThrow(() -> AppException.notFound("Invitation not found"));
        if (!invite.getOrganization().getId().equals(orgId))
            throw AppException.badRequest("Invitation does not belong to this organization");
        invite.setStatus(InviteStatus.CANCELLED);
        invite.setRespondedAt(LocalDateTime.now());
        inviteRepo.save(invite);
        return ApiResponse.ok("Invitation cancelled");
    }

    @Override
    @Transactional
    public ApiResponse<Void> resendInvitation(Long orgId, Long inviteId) {
        Invite invite = inviteRepo.findById(inviteId)
                .orElseThrow(() -> AppException.notFound("Invitation not found"));
        if (!invite.getOrganization().getId().equals(orgId))
            throw AppException.badRequest("Invitation does not belong to this organization");
        invite.setToken(UUID.randomUUID().toString());
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));
        invite.setStatus(InviteStatus.PENDING);
        inviteRepo.save(invite);
        sendInvitationEmail(invite);
        return ApiResponse.ok("Invitation resent");
    }

    @Override
    @Transactional
    public ApiResponse<Void> acceptInvitation(String token) {
        Invite invite = inviteRepo.findByToken(token)
                .orElseThrow(() -> AppException.notFound("Invalid invitation token"));
        if (invite.getStatus() != InviteStatus.PENDING)
            throw AppException.badRequest("Invitation is no longer pending");
        if (invite.getExpiresAt().isBefore(LocalDateTime.now()))
            throw AppException.badRequest("Invitation has expired");

        // Find or note user — if registered, add as member
        userRepo.findByEmail(invite.getEmail()).ifPresent(user -> {
            boolean alreadyMember = memberRepo.findByUserIdAndOrganizationId(
                    user.getId(), invite.getOrganization().getId()).isPresent();
            if (!alreadyMember) {
                TeamMember member = TeamMember.builder()
                        .user(user)
                        .organization(invite.getOrganization())
                        .role(invite.getRole())
                        .build();
                memberRepo.save(member);
            }
        });

        invite.setStatus(InviteStatus.ACCEPTED);
        invite.setRespondedAt(LocalDateTime.now());
        inviteRepo.save(invite);
        return ApiResponse.ok("Invitation accepted");
    }

    @Override
    @Transactional
    public ApiResponse<Void> rejectInvitation(String token) {
        Invite invite = inviteRepo.findByToken(token)
                .orElseThrow(() -> AppException.notFound("Invalid invitation token"));
        if (invite.getStatus() != InviteStatus.PENDING)
            throw AppException.badRequest("Invitation is no longer pending");
        invite.setStatus(InviteStatus.REJECTED);
        invite.setRespondedAt(LocalDateTime.now());
        inviteRepo.save(invite);
        return ApiResponse.ok("Invitation rejected");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void sendInvitationEmail(Invite invite) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(invite.getEmail());
            msg.setSubject("You're invited to join " + invite.getOrganization().getName() + " on NeuroForge");
            msg.setText(
                "Hello,\n\n" +
                "You have been invited to join the organization \"" + invite.getOrganization().getName() + "\" on NeuroForge.\n\n" +
                "Click the link below to accept or reject this invitation:\n\n" +
                "Accept: http://localhost:5173/invitations/accept?token=" + invite.getToken() + "\n" +
                "Reject: http://localhost:5173/invitations/reject?token=" + invite.getToken() + "\n\n" +
                "This invitation expires on " + invite.getExpiresAt() + ".\n\n" +
                "The NeuroForge Team"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", invite.getEmail(), e.getMessage());
        }
    }
}
