package com.forgemind.modules.ai.vector;

import com.forgemind.modules.ai.knowledge.KnowledgeChunk;

/**
 * Pairs a {@link KnowledgeChunk} with its cosine similarity score from a vector search.
 *
 * @param chunk the retrieved chunk (contains text, metadata, sourceType, etc.)
 * @param score cosine similarity in the range [0.0, 1.0]; higher is more relevant
 */
public record ScoredChunk(KnowledgeChunk chunk, double score) {}
