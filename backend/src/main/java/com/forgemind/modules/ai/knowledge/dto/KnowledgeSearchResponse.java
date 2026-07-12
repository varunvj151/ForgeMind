package com.forgemind.modules.ai.knowledge.dto;

import java.util.List;

public record KnowledgeSearchResponse(
    String query,
    long durationMs,
    String embeddingProvider,
    List<ChunkSearchResult> results) {}
