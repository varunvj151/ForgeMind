package com.forgemind.modules.ai.knowledge;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * JPA entity representing a single indexed source object in the knowledge base.
 *
 * <p>Each source entity (project, task, team, etc.) maps to exactly one {@code KnowledgeDocument}.
 * The document tracks the content checksum so unchanged content is never re-embedded. The actual
 * searchable text lives in the associated {@link KnowledgeChunk} records.
 */
@Entity
@Table(name = "knowledge_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 50)
  private KnowledgeSourceType sourceType;

  /** UUID of the originating entity, stored as a string for cross-type flexibility. */
  @Column(name = "source_id", nullable = false, length = 255)
  private String sourceId;

  /** Scopes this document to a project; used for authorization and filtered search. */
  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "title", length = 500)
  private String title;

  /** SHA-256 hex digest of the concatenated chunk texts; skips re-embedding if unchanged. */
  @Column(name = "checksum", length = 64)
  private String checksum;

  @Column(name = "chunk_count", nullable = false)
  private int chunkCount;

  @Column(name = "indexed_at", nullable = false)
  private Instant indexedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
    if (indexedAt == null) {
      indexedAt = now;
    }
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}
