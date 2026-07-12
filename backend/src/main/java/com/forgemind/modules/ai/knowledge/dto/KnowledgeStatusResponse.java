package com.forgemind.modules.ai.knowledge.dto;

import java.util.UUID;

public record KnowledgeStatusResponse(
    UUID projectId,
    long documentCount,
    long chunkCount,
    long pendingEmbeddings) {}
