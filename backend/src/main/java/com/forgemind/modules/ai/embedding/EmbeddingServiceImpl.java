package com.forgemind.modules.ai.embedding;

import com.forgemind.modules.ai.knowledge.KnowledgeChunk;
import com.forgemind.modules.ai.observability.KnowledgeMetrics;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default implementation of {@link EmbeddingService}. */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

  private final EmbeddingProvider embeddingProvider;
  private final JdbcTemplate jdbcTemplate;
  private final EmbeddingProperties embeddingProperties;
  private final KnowledgeMetrics knowledgeMetrics;

  @Override
  @Async
  @Transactional
  public void embedChunk(KnowledgeChunk chunk) {
    embedChunks(List.of(chunk));
  }

  @Override
  @Async
  @Transactional
  public void embedChunks(List<KnowledgeChunk> chunks) {
    if (chunks.isEmpty()) {
      return;
    }
    int batchSize = embeddingProperties.getBatchSize();
    List<List<KnowledgeChunk>> batches = partition(chunks, batchSize);

    for (List<KnowledgeChunk> batch : batches) {
      long start = System.currentTimeMillis();
      try {
        List<String> texts = batch.stream().map(KnowledgeChunk::getChunkText).toList();
        List<float[]> vectors = embeddingProvider.embedBatch(texts);

        for (int i = 0; i < batch.size(); i++) {
          persistEmbedding(batch.get(i).getId(), vectors.get(i));
        }
        long elapsed = System.currentTimeMillis() - start;
        knowledgeMetrics.recordEmbeddingSuccess(embeddingProvider.providerName(), elapsed, batch.size());
        log.debug(
            "Embedded batch of {} chunks in {}ms via {}",
            batch.size(),
            elapsed,
            embeddingProvider.providerName());
      } catch (Exception e) {
        knowledgeMetrics.recordEmbeddingFailure(embeddingProvider.providerName(), e.getClass().getSimpleName());
        log.error("Failed to embed batch of {} chunks: {}", batch.size(), e.getMessage(), e);
        throw e;
      }
    }
  }

  @Override
  @Transactional
  public void clearEmbeddingsForDocument(UUID documentId) {
    jdbcTemplate.update(
        "UPDATE knowledge_chunks SET embedding = NULL WHERE document_id = ?", documentId);
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private void persistEmbedding(UUID chunkId, float[] vector) {
    String sql = "UPDATE knowledge_chunks SET embedding = ?::vector WHERE id = ?";
    jdbcTemplate.update(sql, toVectorLiteral(vector), chunkId);
  }

  /**
   * Converts a float array to pgvector's text literal format: {@code [v1,v2,...,vN]}.
   * This string can be cast with {@code ::vector} in any PostgreSQL statement.
   */
  static String toVectorLiteral(float[] vector) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < vector.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(vector[i]);
    }
    sb.append(']');
    return sb.toString();
  }

  private static <T> List<List<T>> partition(List<T> list, int size) {
    List<List<T>> result = new ArrayList<>();
    for (int i = 0; i < list.size(); i += size) {
      result.add(list.subList(i, Math.min(i + size, list.size())));
    }
    return result;
  }
}
