package com.forgemind.modules.git.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitRepositoryResponse {
  private UUID id;
  private UUID projectId;
  private String provider;
  private String owner;
  private String repoName;
  private String fullName;
  private String defaultBranch;
  private String cloneUrl;
  private String visibility;
  private String primaryLanguage;
  private String description;
  private Instant lastSyncAt;
  private boolean webhookActive;
  private Instant createdAt;
  private Instant updatedAt;
}
