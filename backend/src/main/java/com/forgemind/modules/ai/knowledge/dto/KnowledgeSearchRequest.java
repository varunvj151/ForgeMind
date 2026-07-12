package com.forgemind.modules.ai.knowledge.dto;

import com.forgemind.modules.ai.knowledge.KnowledgeSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record KnowledgeSearchRequest(
    @NotNull UUID projectId,
    @NotBlank String query,
    KnowledgeSourceType sourceTypeFilter,
    Integer topK,
    Double minScore) {}
