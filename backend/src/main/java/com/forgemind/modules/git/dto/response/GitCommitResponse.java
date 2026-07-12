package com.forgemind.modules.git.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitCommitResponse {
  private UUID id;
  private UUID repositoryId;
  private String sha;
  private String message;
  private String authorName;
  private String authorEmail;
  private Instant authoredAt;
  private String branchName;
  private int filesChanged;
  private int additions;
  private int deletions;
  private Instant syncedAt;
}
