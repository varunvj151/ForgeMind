package com.forgemind.modules.ai.rag.dto;

import com.forgemind.modules.ai.knowledge.dto.ChunkSearchResult;
import java.util.List;

public record RagQueryResponse(
    String originalPrompt,
    String augmentedPrompt,
    boolean wasAugmented,
    List<ChunkSearchResult> retrievedContext) {}
