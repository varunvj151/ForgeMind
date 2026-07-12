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
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a chunk of source code indexed for vector search.
 * Note: the actual 'embedding' column is managed natively to support pgvector,
 * so it isn't mapped directly to a JPA field here unless a custom Hibernate type is used.
 */
@Entity
@Table(name = "code_chunks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeChunk {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "repository_id", nullable = false)
  private GitRepository repository;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "file_path", nullable = false, length = 1024)
  private String filePath;

  @Column(name = "language", nullable = false, length = 30)
  private String language;

  @Column(name = "symbol_name", length = 500)
  private String symbolName;

  @Column(name = "chunk_index", nullable = false)
  private int chunkIndex;

  @Column(name = "chunk_text", nullable = false, columnDefinition = "TEXT")
  private String chunkText;

  @Column(name = "metadata", columnDefinition = "JSONB")
  private String metadata; // JSON representation of metadata

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
