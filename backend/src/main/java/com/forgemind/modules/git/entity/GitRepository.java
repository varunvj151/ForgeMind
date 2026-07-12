package com.forgemind.modules.git.entity;

import com.forgemind.modules.git.provider.GitProviderType;
import com.forgemind.modules.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Represents a connected Git repository linked to a ForgeMind project.
 */
@Entity
@Table(name = "git_repositories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitRepository {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Enumerated(EnumType.STRING)
  @Column(name = "provider", nullable = false, length = 30)
  private GitProviderType provider;

  @Column(name = "owner", nullable = false, length = 255)
  private String owner;

  @Column(name = "repo_name", nullable = false, length = 255)
  private String repoName;

  @Column(name = "full_name", nullable = false, length = 512)
  private String fullName;

  @Column(name = "default_branch", length = 255)
  private String defaultBranch;

  @Column(name = "clone_url", length = 1024)
  private String cloneUrl;

  @Column(name = "visibility", length = 20)
  private String visibility;

  @Column(name = "primary_language", length = 50)
  private String primaryLanguage;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "last_sync_at")
  private Instant lastSyncAt;

  @Column(name = "last_commit_sha", length = 40)
  private String lastCommitSha;

  @Column(name = "webhook_secret", length = 255)
  private String webhookSecret;

  @Column(name = "webhook_active")
  private boolean webhookActive;

  @Column(name = "access_token", columnDefinition = "TEXT")
  private String accessToken;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
    if (fullName == null && owner != null && repoName != null) {
      fullName = owner + "/" + repoName;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
    if (fullName == null && owner != null && repoName != null) {
      fullName = owner + "/" + repoName;
    }
  }
}
