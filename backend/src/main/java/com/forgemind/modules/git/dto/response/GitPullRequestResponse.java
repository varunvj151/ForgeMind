package com.forgemind.modules.git.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitPullRequestResponse {
  private UUID id;
  private UUID repositoryId;
  private int prNumber;
  private String title;
  private String description;
  private String state;
  private String authorLogin;
  private String sourceBranch;
  private String targetBranch;
  private int filesChanged;
  private int additions;
  private int deletions;
  private Instant mergedAt;
  private Instant closedAt;
  private boolean aiReviewed;
  private String aiReviewResult;
  private Instant createdAt;
  private Instant updatedAt;
}
