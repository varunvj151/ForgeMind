package com.forgemind.modules.ai.knowledge.dto;

import com.forgemind.modules.ai.knowledge.KnowledgeSourceType;
import java.util.UUID;

public record ChunkSearchResult(
    UUID chunkId,
    KnowledgeSourceType sourceType,
    String sourceId,
    String text,
    String metadata,
    double score) {}
