package com.forgemind.modules.ai.knowledge;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * JPA entity representing a single text chunk derived from a {@link KnowledgeDocument}.
 *
 * <p>The {@code embedding} column (pgvector {@code vector(1536)}) is intentionally excluded from
 * this JPA entity. Vector reads and writes are performed exclusively via native JDBC in {@link
 * com.forgemind.modules.ai.vector.PgVectorStore}, which formats float arrays as pgvector literal
 * strings (e.g. {@code '[0.1,0.2,…]'}) and uses the {@code ::vector} SQL cast. This avoids
 * any need for a custom Hibernate type or an external ORM extension.
 */
@Entity
@Table(name = "knowledge_chunks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeChunk {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "document_id", nullable = false)
  private UUID documentId;

  /** Denormalized from the parent document for fast project-scoped vector queries. */
  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 50)
  private KnowledgeSourceType sourceType;

  @Column(name = "source_id", nullable = false, length = 255)
  private String sourceId;

  /** 0-based position of this chunk within its parent document. */
  @Column(name = "chunk_index", nullable = false)
  private int chunkIndex;

  @Column(name = "chunk_text", nullable = false, columnDefinition = "TEXT")
  private String chunkText;

  /**
   * Arbitrary JSON metadata stored as a plain string (title, status, priority, assignee, etc.).
   * Stored as JSONB in PostgreSQL for future query flexibility.
   */
  @Column(name = "metadata", columnDefinition = "JSONB")
  private String metadata;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
