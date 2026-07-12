package com.forgemind.modules.git.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a pull request in a connected Git repository.
 */
@Entity
@Table(name = "git_pull_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitPullRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "repository_id", nullable = false)
  private GitRepository repository;

  @Column(name = "pr_number", nullable = false)
  private int prNumber;

  @Column(name = "title", columnDefinition = "TEXT")
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "state", nullable = false, length = 20)
  @Builder.Default
  private String state = "OPEN";

  @Column(name = "author_login", length = 255)
  private String authorLogin;

  @Column(name = "source_branch", length = 255)
  private String sourceBranch;

  @Column(name = "target_branch", length = 255)
  private String targetBranch;

  @Column(name = "files_changed")
  private int filesChanged;

  @Column(name = "additions")
  private int additions;

  @Column(name = "deletions")
  private int deletions;

  @Column(name = "merged_at")
  private Instant mergedAt;

  @Column(name = "closed_at")
  private Instant closedAt;

  @Column(name = "ai_reviewed")
  private boolean aiReviewed;

  @Column(name = "ai_review_result", columnDefinition = "TEXT")
  private String aiReviewResult;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}
