package com.forgemind.modules.organization.security;

import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import com.forgemind.modules.organization.entity.OrganizationMember;
import com.forgemind.modules.organization.entity.OrganizationMemberRole;
import com.forgemind.modules.organization.repository.OrganizationMemberRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Central authorization service for organization-scoped operations.
 *
 * <p>Designed to be used in {@code @PreAuthorize} SpEL expressions:
 * <pre>{@code
 * @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
 * }</pre>
 */
@Slf4j
@Service("orgSecurity")
@RequiredArgsConstructor
public class OrganizationSecurityService {

  private final CurrentUserProvider currentUserProvider;
  private final OrganizationMemberRepository memberRepository;

  /**
   * Returns true if the current user has at least the given role in the organization.
   *
   * @param organizationId the organization to check
   * @param roleName       name of the minimum required {@link OrganizationMemberRole}
   * @return true if the user's role meets or exceeds the required role
   */
  public boolean hasRole(UUID organizationId, String roleName) {
    User user = currentUserProvider.getCurrentUser();
    OrganizationMemberRole required = OrganizationMemberRole.valueOf(roleName);
    Optional<OrganizationMember> member =
        memberRepository.findByOrganizationIdAndUserId(organizationId, user.getId());
    if (member.isEmpty()) return false;
    return hasAtLeastRole(member.get().getRole(), required);
  }

  /**
   * Asserts membership and returns the member, or throws {@link AccessDeniedException}.
   */
  public OrganizationMember requireMembership(UUID organizationId) {
    User user = currentUserProvider.getCurrentUser();
    return memberRepository
        .findByOrganizationIdAndUserId(organizationId, user.getId())
        .orElseThrow(() -> new AccessDeniedException(
            "User " + user.getId() + " is not a member of organization " + organizationId));
  }

  // OWNER > ADMIN > MANAGER > MEMBER > VIEWER
  private boolean hasAtLeastRole(OrganizationMemberRole actual, OrganizationMemberRole required) {
    return actual.ordinal() <= required.ordinal();
  }
}
