package com.forgemind.modules.ai.indexing;

import com.forgemind.modules.ai.knowledge.KnowledgeChunk;
import com.forgemind.modules.ai.knowledge.KnowledgeDocument;
import java.util.List;

/**
 * Carrier produced by {@link KnowledgeChunker} holding a parent document and its derived chunks.
 *
 * @param document the parent {@link KnowledgeDocument} (not yet persisted)
 * @param chunks   the derived {@link KnowledgeChunk} list (not yet persisted)
 */
public record DocumentChunks(KnowledgeDocument document, List<KnowledgeChunk> chunks) {}
