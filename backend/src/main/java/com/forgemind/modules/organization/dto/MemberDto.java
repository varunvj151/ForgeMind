package com.forgemind.modules.organization.dto;

import com.forgemind.modules.organization.entity.OrganizationMemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/** DTO container for organization member operations. */
public final class MemberDto {

  private MemberDto() {}

  public record InviteMemberRequest(
      @NotBlank @Email String email,
      @NotNull OrganizationMemberRole role
  ) {}

  public record UpdateMemberRoleRequest(
      @NotNull OrganizationMemberRole role
  ) {}

  public record MemberResponse(
      UUID id,
      Long userId,
      String username,
      String email,
      String firstName,
      String lastName,
      OrganizationMemberRole role,
      Instant joinedAt
  ) {}

  public record InvitationResponse(
      UUID id,
      UUID organizationId,
      String email,
      OrganizationMemberRole role,
      String status,
      Instant expiresAt,
      Instant createdAt
  ) {}
}
