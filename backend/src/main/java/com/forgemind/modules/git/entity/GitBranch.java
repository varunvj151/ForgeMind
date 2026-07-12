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
 * Represents a branch in a connected Git repository.
 */
@Entity
@Table(name = "git_branches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitBranch {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "repository_id", nullable = false)
  private GitRepository repository;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "commit_sha", length = 40)
  private String commitSha;

  @Column(name = "is_default")
  private boolean isDefault;

  @Column(name = "is_protected")
  private boolean isProtected;

  @Column(name = "synced_at", nullable = false)
  @Builder.Default
  private Instant syncedAt = Instant.now();
}
