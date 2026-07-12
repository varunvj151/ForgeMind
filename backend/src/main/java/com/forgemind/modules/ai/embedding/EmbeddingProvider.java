package com.forgemind.modules.ai.embedding;

import java.util.List;

/**
 * Abstraction over any embedding model provider.
 *
 * <p>Implementations convert text into dense float vectors of a fixed dimension. The RAG pipeline
 * depends only on this interface — never on a vendor SDK or HTTP client — so the underlying provider
 * can be swapped (Mock → Gemini → OpenAI → Ollama) without changing calling code.
 *
 * <p>All returned vectors are expected to be L2-normalized (unit vectors) so that cosine similarity
 * equals the dot product and the pgvector {@code <=>} cosine distance operator is correct.
 */
public interface EmbeddingProvider {

  /** Stable identifier for this provider (used in metrics tags). */
  String providerName();

  /**
   * Generates an embedding vector for a single text input.
   *
   * @param text the input text (should be non-null and non-blank)
   * @return a float array of length {@link #dimensions()}, L2-normalized
   */
  float[] embed(String text);

  /**
   * Generates embedding vectors for a batch of text inputs.
   *
   * <p>Default implementation calls {@link #embed(String)} for each element; providers that
   * support native batching should override this for efficiency.
   *
   * @param texts list of input texts; must not be null or empty
   * @return list of float arrays, same order and size as {@code texts}
   */
  default List<float[]> embedBatch(List<String> texts) {
    return texts.stream().map(this::embed).toList();
  }

  /** The fixed number of dimensions produced by this provider. Must match the DB schema. */
  int dimensions();
}
