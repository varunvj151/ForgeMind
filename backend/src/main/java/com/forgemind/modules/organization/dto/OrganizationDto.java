package com.forgemind.modules.organization.dto;

import com.forgemind.modules.organization.entity.OrganizationPlan;
import com.forgemind.modules.organization.entity.OrganizationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/** DTO container class for Organization request/response objects. */
public final class OrganizationDto {

  private OrganizationDto() {}

  /** Request to create a new organization. */
  public record CreateOrganizationRequest(
      @NotBlank @Size(min = 2, max = 255) String name,
      @NotBlank @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase alphanumeric with hyphens")
      @Size(min = 2, max = 100) String slug,
      String logoUrl
  ) {}

  /** Request to update an existing organization. */
  public record UpdateOrganizationRequest(
      @Size(min = 2, max = 255) String name,
      String logoUrl
  ) {}

  /** Response payload for an organization. */
  public record OrganizationResponse(
      UUID id,
      String name,
      String slug,
      String logoUrl,
      OrganizationPlan plan,
      OrganizationStatus status,
      Long ownerId,
      Instant createdAt
  ) {}
}
