package com.forgemind.modules.ai.vector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.knowledge.KnowledgeChunk;
import com.forgemind.modules.ai.knowledge.KnowledgeSourceType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * PostgreSQL + pgvector implementation of {@link VectorStore}.
 *
 * <p>All vector operations use native JDBC with pgvector's literal string format ({@code
 * '[v1,v2,…]'::vector}) and the cosine distance operator ({@code <=>}). No Hibernate vector type or
 * external ORM extension is required — only {@link JdbcTemplate} and the pgvector PostgreSQL
 * extension (enabled in V9 migration).
 *
 * <p>The IVFFlat index ({@code idx_knowledge_chunks_embedding}) accelerates approximate nearest-
 * neighbour search for large datasets. Below ~1 000 rows exact scan is used automatically.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PgVectorStore implements VectorStore {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public List<ScoredChunk> search(float[] queryVector, UUID projectId, int topK) {
    String vectorLiteral = toVectorLiteral(queryVector);
    String sql =
        """
        SELECT kc.id, kc.document_id, kc.project_id, kc.source_type, kc.source_id,
               kc.chunk_index, kc.chunk_text, kc.metadata, kc.created_at,
               1 - (kc.embedding <=> ?::vector) AS score
        FROM   knowledge_chunks kc
        WHERE  kc.project_id = ?
          AND  kc.embedding  IS NOT NULL
        ORDER  BY kc.embedding <=> ?::vector
        LIMIT  ?
        """;
    return jdbcTemplate.query(
        sql, this::mapRow, vectorLiteral, projectId, vectorLiteral, topK);
  }

  @Override
  public List<ScoredChunk> searchWithFilter(
      float[] queryVector, UUID projectId, KnowledgeSourceType sourceType, int topK) {
    if (sourceType == null) {
      return search(queryVector, projectId, topK);
    }
    String vectorLiteral = toVectorLiteral(queryVector);
    String sql =
        """
        SELECT kc.id, kc.document_id, kc.project_id, kc.source_type, kc.source_id,
               kc.chunk_index, kc.chunk_text, kc.metadata, kc.created_at,
               1 - (kc.embedding <=> ?::vector) AS score
        FROM   knowledge_chunks kc
        WHERE  kc.project_id = ?
          AND  kc.source_type = ?
          AND  kc.embedding   IS NOT NULL
        ORDER  BY kc.embedding <=> ?::vector
        LIMIT  ?
        """;
    return jdbcTemplate.query(
        sql, this::mapRow, vectorLiteral, projectId, sourceType.name(), vectorLiteral, topK);
  }

  @Override
  public void saveEmbedding(UUID chunkId, float[] embedding) {
    String sql = "UPDATE knowledge_chunks SET embedding = ?::vector WHERE id = ?";
    jdbcTemplate.update(sql, toVectorLiteral(embedding), chunkId);
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  public static String toVectorLiteral(float[] vector) {
    if (vector == null || vector.length == 0) return "[]";
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < vector.length; i++) {
      sb.append(vector[i]);
      if (i < vector.length - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  private ScoredChunk mapRow(ResultSet rs, int rowNum) throws SQLException {
    KnowledgeChunk chunk = new KnowledgeChunk();
    chunk.setId(UUID.fromString(rs.getString("id")));
    chunk.setDocumentId(UUID.fromString(rs.getString("document_id")));
    chunk.setProjectId(UUID.fromString(rs.getString("project_id")));
    chunk.setSourceType(KnowledgeSourceType.valueOf(rs.getString("source_type")));
    chunk.setSourceId(rs.getString("source_id"));
    chunk.setChunkIndex(rs.getInt("chunk_index"));
    chunk.setChunkText(rs.getString("chunk_text"));
    chunk.setMetadata(rs.getString("metadata"));
    java.sql.Timestamp ts = rs.getTimestamp("created_at");
    if (ts != null) {
      chunk.setCreatedAt(ts.toInstant());
    }
    double score = rs.getDouble("score");
    return new ScoredChunk(chunk, score);
  }
}
