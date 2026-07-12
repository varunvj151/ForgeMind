package com.forgemind.modules.ai.knowledge.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record KnowledgeReindexRequest(
    @NotNull UUID projectId) {}
