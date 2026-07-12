package com.forgemind.modules.ai.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RagQueryRequest(
    @NotNull UUID projectId,
    @NotBlank String prompt) {}
