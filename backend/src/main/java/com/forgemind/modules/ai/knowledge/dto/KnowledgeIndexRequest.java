package com.forgemind.modules.ai.knowledge.dto;

import com.forgemind.modules.ai.knowledge.KnowledgeSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record KnowledgeIndexRequest(
    @NotNull KnowledgeSourceType sourceType,
    @NotBlank String sourceId,
    @NotNull UUID projectId,
    @NotBlank String title,
    @NotBlank String content) {}
