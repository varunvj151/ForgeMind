package com.forgemind.modules.ai.retrieval;

import com.forgemind.modules.ai.vector.ScoredChunk;
import java.util.List;

/**
 * Result of a semantic search, containing the retrieved chunks and metrics.
 *
 * @param chunks        the ordered list of retrieved chunks (highest score first)
 * @param durationMs    time taken to embed the query and search the vector store
 * @param providerName  the embedding provider used for the query
 */
public record RetrievalResult(
    List<ScoredChunk> chunks,
    long durationMs,
    String providerName) {}
