package com.forgemind.modules.ai.vector;

import com.forgemind.modules.ai.knowledge.KnowledgeSourceType;
import java.util.List;
import java.util.UUID;

/**
 * Abstraction over the vector similarity search backend.
 *
 * <p>The RAG retrieval engine depends only on this interface, so the storage technology can be
 * changed (PgVector → Qdrant, Weaviate, etc.) without touching retrieval logic.
 */
public interface VectorStore {

  /**
   * Finds the {@code topK} chunks most similar to the query vector within the given project.
   *
   * @param queryVector the embedded query (must have the same dimension as stored embeddings)
   * @param projectId restricts the search to this project (mandatory — no cross-project leakage)
   * @param topK maximum number of results to return
   * @return scored chunks ordered by descending similarity
   */
  List<ScoredChunk> search(float[] queryVector, UUID projectId, int topK);

  /**
   * Like {@link #search} but further filters to a single source type.
   *
   * @param sourceType if {@code null}, behaves identically to {@link #search}
   */
  List<ScoredChunk> searchWithFilter(
      float[] queryVector, UUID projectId, KnowledgeSourceType sourceType, int topK);

  /**
   * Saves the embedding vector for a specific chunk (identified by its UUID).
   *
   * <p>Called by {@link com.forgemind.modules.ai.embedding.EmbeddingService} after generation.
   */
  void saveEmbedding(UUID chunkId, float[] embedding);
}
