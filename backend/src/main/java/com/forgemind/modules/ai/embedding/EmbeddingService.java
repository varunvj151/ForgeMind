package com.forgemind.modules.ai.embedding;

import com.forgemind.modules.ai.knowledge.KnowledgeChunk;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for generating and persisting embeddings for knowledge chunks.
 *
 * <p>Delegates vector generation to the active {@link EmbeddingProvider} and persists the result
 * via native JDBC (not JPA) to avoid the need for a Hibernate vector type adapter.
 */
public interface EmbeddingService {

  /** Generates and persists the embedding for a single chunk. */
  void embedChunk(KnowledgeChunk chunk);

  /** Generates and persists embeddings for a batch of chunks. */
  void embedChunks(List<KnowledgeChunk> chunks);

  /** Deletes all embeddings for chunks belonging to the given document. */
  void clearEmbeddingsForDocument(UUID documentId);
}
