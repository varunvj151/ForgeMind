package com.forgemind.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/** DTO container class for Workspace request/response objects. */
public final class WorkspaceDto {

  private WorkspaceDto() {}

  public record CreateWorkspaceRequest(
      @NotBlank @Size(min = 2, max = 255) String name,
      @NotBlank @Pattern(regexp = "^[a-z0-9-]+$") @Size(min = 2, max = 100) String slug
  ) {}

  public record WorkspaceResponse(
      UUID id,
      UUID organizationId,
      String name,
      String slug,
      Instant createdAt
  ) {}
}
