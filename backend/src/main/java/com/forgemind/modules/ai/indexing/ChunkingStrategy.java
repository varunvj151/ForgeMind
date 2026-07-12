package com.forgemind.modules.ai.indexing;

import java.util.List;

/**
 * Strategy for splitting a text document into overlapping chunks suitable for embedding.
 *
 * <p>Implementations should produce chunks that:
 * <ul>
 *   <li>Fit within the embedding model's token limit (typically 512–8192 tokens)
 *   <li>Overlap slightly with adjacent chunks to preserve inter-sentence context
 *   <li>Avoid splitting mid-sentence where possible
 * </ul>
 */
public interface ChunkingStrategy {

  /**
   * Splits the input content into a list of text chunks.
   *
   * @param content the full text to chunk (may be empty or blank — returns empty list)
   * @param options tuning parameters (max size, overlap, min size)
   * @return ordered list of text chunks; never null
   */
  List<String> chunk(String content, ChunkingOptions options);
}
