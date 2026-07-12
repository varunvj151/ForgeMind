package com.forgemind.modules.git.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a commit in a connected Git repository.
 */
@Entity
@Table(name = "git_commits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitCommit {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "repository_id", nullable = false)
  private GitRepository repository;

  @Column(name = "sha", nullable = false, length = 40)
  private String sha;

  @Column(name = "message", columnDefinition = "TEXT")
  private String message;

  @Column(name = "author_name", length = 255)
  private String authorName;

  @Column(name = "author_email", length = 255)
  private String authorEmail;

  @Column(name = "authored_at")
  private Instant authoredAt;

  @Column(name = "branch_name", length = 255)
  private String branchName;

  @Column(name = "files_changed")
  private int filesChanged;

  @Column(name = "additions")
  private int additions;

  @Column(name = "deletions")
  private int deletions;

  @Column(name = "synced_at", nullable = false)
  @Builder.Default
  private Instant syncedAt = Instant.now();
}
