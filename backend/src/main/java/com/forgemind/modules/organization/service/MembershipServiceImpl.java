package com.forgemind.modules.organization.service;

import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.repository.UserRepository;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import com.forgemind.modules.organization.dto.MemberDto.InvitationResponse;
import com.forgemind.modules.organization.dto.MemberDto.InviteMemberRequest;
import com.forgemind.modules.organization.dto.MemberDto.MemberResponse;
import com.forgemind.modules.organization.dto.MemberDto.UpdateMemberRoleRequest;
import com.forgemind.modules.organization.entity.InvitationStatus;
import com.forgemind.modules.organization.entity.Organization;
import com.forgemind.modules.organization.entity.OrganizationInvitation;
import com.forgemind.modules.organization.entity.OrganizationMember;
import com.forgemind.modules.organization.repository.OrganizationInvitationRepository;
import com.forgemind.modules.organization.repository.OrganizationMemberRepository;
import com.forgemind.modules.organization.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

  private static final int INVITATION_EXPIRY_DAYS = 7;
  private final SecureRandom secureRandom = new SecureRandom();

  private final OrganizationRepository organizationRepository;
  private final OrganizationMemberRepository memberRepository;
  private final OrganizationInvitationRepository invitationRepository;
  private final UserRepository userRepository;
  private final CurrentUserProvider currentUserProvider;
  private final EmailService emailService;

  @Override
  @Transactional(readOnly = true)
  public List<MemberResponse> listMembers(UUID organizationId) {
    return memberRepository.findAllByOrganizationId(organizationId).stream()
        .map(this::toMemberResponse)
        .toList();
  }

  @Override
  @Transactional
  public InvitationResponse inviteMember(UUID organizationId, InviteMemberRequest request) {
    Organization org = findOrgOrThrow(organizationId);
    User inviter = currentUserProvider.getCurrentUser();

    // Generate a secure random raw token
    byte[] rawBytes = new byte[32];
    secureRandom.nextBytes(rawBytes);
    String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(rawBytes);
    String tokenHash = sha256Hex(rawToken);

    OrganizationInvitation invitation = OrganizationInvitation.builder()
        .organization(org)
        .email(request.email())
        .tokenHash(tokenHash)
        .role(request.role())
        .invitedBy(inviter)
        .expiresAt(Instant.now().plus(INVITATION_EXPIRY_DAYS, ChronoUnit.DAYS))
        .build();

    invitation = invitationRepository.save(invitation);
    emailService.sendInvitation(request.email(), org.getName(), rawToken);
    log.info("Invitation sent: org={}, email={}, invitedBy={}", organizationId, request.email(), inviter.getId());
    return toInvitationResponse(invitation);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InvitationResponse> listInvitations(UUID organizationId) {
    return invitationRepository
        .findAllByOrganizationIdAndStatus(organizationId, InvitationStatus.PENDING).stream()
        .map(this::toInvitationResponse)
        .toList();
  }

  @Override
  @Transactional
  public MemberResponse acceptInvitation(String rawToken) {
    String tokenHash = sha256Hex(rawToken);
    OrganizationInvitation invitation = invitationRepository.findByTokenHash(tokenHash)
        .orElseThrow(() -> new EntityNotFoundException("Invitation not found or already used"));

    if (!invitation.isValid()) {
      throw new IllegalStateException("Invitation is expired or no longer valid");
    }

    User user = currentUserProvider.getCurrentUser();
    Organization org = invitation.getOrganization();

    if (memberRepository.existsByOrganizationIdAndUserId(org.getId(), user.getId())) {
      throw new IllegalStateException("User is already a member of this organization");
    }

    OrganizationMember member = OrganizationMember.builder()
        .organization(org)
        .user(user)
        .role(invitation.getRole())
        .build();
    member = memberRepository.save(member);

    invitation.setStatus(InvitationStatus.ACCEPTED);
    invitationRepository.save(invitation);

    log.info("Invitation accepted: org={}, user={}", org.getId(), user.getId());
    return toMemberResponse(member);
  }

  @Override
  @Transactional
  public void cancelInvitation(UUID organizationId, UUID invitationId) {
    OrganizationInvitation invitation = invitationRepository.findById(invitationId)
        .filter(i -> i.getOrganization().getId().equals(organizationId))
        .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));
    invitation.setStatus(InvitationStatus.CANCELLED);
    invitationRepository.save(invitation);
  }

  @Override
  @Transactional
  public MemberResponse updateMemberRole(UUID organizationId, Long userId, UpdateMemberRoleRequest request) {
    OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
        .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    if (member.getRole().name().equals("OWNER")) {
      throw new AccessDeniedException("Cannot change the role of the organization owner");
    }
    member.setRole(request.role());
    return toMemberResponse(memberRepository.save(member));
  }

  @Override
  @Transactional
  public void removeMember(UUID organizationId, Long userId) {
    OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
        .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    if (member.getRole().name().equals("OWNER")) {
      throw new AccessDeniedException("Cannot remove the organization owner");
    }
    memberRepository.deleteByOrganizationIdAndUserId(organizationId, userId);
    log.info("Member removed: org={}, user={}", organizationId, userId);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private Organization findOrgOrThrow(UUID id) {
    return organizationRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + id));
  }

  private String sha256Hex(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private MemberResponse toMemberResponse(OrganizationMember m) {
    User u = m.getUser();
    return new MemberResponse(m.getId(), u.getId(), u.getUsername(), u.getEmail(),
        u.getFirstName(), u.getLastName(), m.getRole(), m.getJoinedAt());
  }

  private InvitationResponse toInvitationResponse(OrganizationInvitation i) {
    return new InvitationResponse(i.getId(), i.getOrganization().getId(), i.getEmail(),
        i.getRole(), i.getStatus().name(), i.getExpiresAt(), i.getCreatedAt());
  }
}
