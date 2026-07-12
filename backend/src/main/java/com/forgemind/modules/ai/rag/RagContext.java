package com.forgemind.modules.ai.rag;

import com.forgemind.modules.ai.vector.ScoredChunk;
import java.util.List;

/**
 * The contextual payload retrieved by the RAG orchestrator, ready to be injected into an LLM prompt.
 *
 * @param query          the original natural language question/prompt that triggered retrieval
 * @param retrievedChunks the semantic search results
 * @param augmentedPrompt the final prompt string with chunks appended (if augmented)
 * @param wasAugmented    true if the retrieval engine was enabled and chunks were found
 */
public record RagContext(
    String query,
    List<ScoredChunk> retrievedChunks,
    String augmentedPrompt,
    boolean wasAugmented) {}
