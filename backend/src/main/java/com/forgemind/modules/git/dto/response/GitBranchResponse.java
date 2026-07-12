package com.forgemind.modules.git.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitBranchResponse {
  private UUID id;
  private UUID repositoryId;
  private String name;
  private String commitSha;
  private boolean isDefault;
  private boolean isProtected;
  private Instant syncedAt;
}
