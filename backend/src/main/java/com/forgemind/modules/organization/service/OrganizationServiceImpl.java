package com.forgemind.modules.organization.service;

import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import com.forgemind.modules.organization.dto.OrganizationDto.CreateOrganizationRequest;
import com.forgemind.modules.organization.dto.OrganizationDto.OrganizationResponse;
import com.forgemind.modules.organization.dto.OrganizationDto.UpdateOrganizationRequest;
import com.forgemind.modules.organization.entity.Organization;
import com.forgemind.modules.organization.entity.OrganizationMember;
import com.forgemind.modules.organization.entity.OrganizationMemberRole;
import com.forgemind.modules.organization.entity.OrganizationStatus;
import com.forgemind.modules.organization.repository.OrganizationMemberRepository;
import com.forgemind.modules.organization.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
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
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final OrganizationMemberRepository memberRepository;
  private final CurrentUserProvider currentUserProvider;

  @Override
  @Transactional
  public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
    if (organizationRepository.existsBySlug(request.slug())) {
      throw new IllegalArgumentException("Organization slug already taken: " + request.slug());
    }
    User owner = currentUserProvider.getCurrentUser();
    Organization org = Organization.builder()
        .name(request.name())
        .slug(request.slug())
        .logoUrl(request.logoUrl())
        .owner(owner)
        .build();
    org = organizationRepository.save(org);

    // Auto-add owner as OWNER member
    OrganizationMember ownerMember = OrganizationMember.builder()
        .organization(org)
        .user(owner)
        .role(OrganizationMemberRole.OWNER)
        .build();
    memberRepository.save(ownerMember);

    log.info("Organization created: id={}, slug={}, owner={}", org.getId(), org.getSlug(), owner.getId());
    return toResponse(org);
  }

  @Override
  @Transactional(readOnly = true)
  public OrganizationResponse getOrganizationById(UUID id) {
    return toResponse(findOrThrow(id));
  }

  @Override
  @Transactional(readOnly = true)
  public OrganizationResponse getOrganizationBySlug(String slug) {
    Organization org = organizationRepository.findBySlug(slug)
        .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + slug));
    return toResponse(org);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrganizationResponse> getMyOrganizations() {
    User user = currentUserProvider.getCurrentUser();
    return memberRepository.findAllByUserId(user.getId()).stream()
        .map(m -> toResponse(m.getOrganization()))
        .toList();
  }

  @Override
  @Transactional
  public OrganizationResponse updateOrganization(UUID id, UpdateOrganizationRequest request) {
    Organization org = findOrThrow(id);
    requireAtLeastAdmin(org);
    if (request.name() != null) org.setName(request.name());
    if (request.logoUrl() != null) org.setLogoUrl(request.logoUrl());
    org = organizationRepository.save(org);
    log.info("Organization updated: id={}", id);
    return toResponse(org);
  }

  @Override
  @Transactional
  public void deleteOrganization(UUID id) {
    Organization org = findOrThrow(id);
    requireOwner(org);
    org.setStatus(OrganizationStatus.DELETED);
    organizationRepository.save(org);
    log.info("Organization soft-deleted: id={}", id);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private Organization findOrThrow(UUID id) {
    return organizationRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + id));
  }

  private void requireAtLeastAdmin(Organization org) {
    User user = currentUserProvider.getCurrentUser();
    memberRepository.findByOrganizationIdAndUserId(org.getId(), user.getId())
        .filter(m -> m.getRole() == OrganizationMemberRole.OWNER
            || m.getRole() == OrganizationMemberRole.ADMIN)
        .orElseThrow(() -> new AccessDeniedException("Admin or Owner role required"));
  }

  private void requireOwner(Organization org) {
    User user = currentUserProvider.getCurrentUser();
    if (!org.getOwner().getId().equals(user.getId())) {
      throw new AccessDeniedException("Only the organization owner can delete it");
    }
  }

  private OrganizationResponse toResponse(Organization org) {
    return new OrganizationResponse(
        org.getId(), org.getName(), org.getSlug(), org.getLogoUrl(),
        org.getPlan(), org.getStatus(), org.getOwner().getId(), org.getCreatedAt());
  }
}
