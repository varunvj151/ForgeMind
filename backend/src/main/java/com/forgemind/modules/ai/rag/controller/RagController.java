package com.forgemind.modules.ai.rag.controller;

import com.forgemind.modules.ai.agent.security.AgentAccessGuard;
import com.forgemind.modules.ai.knowledge.dto.ChunkSearchResult;
import com.forgemind.modules.ai.rag.RagContext;
import com.forgemind.modules.ai.rag.RagOrchestrator;
import com.forgemind.modules.ai.rag.dto.RagQueryRequest;
import com.forgemind.modules.ai.rag.dto.RagQueryResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for testing RAG prompt augmentation directly. */
@RestController
@RequestMapping("/api/v1/ai/rag")
@RequiredArgsConstructor
public class RagController {

  private final RagOrchestrator ragOrchestrator;
  private final AgentAccessGuard accessGuard;

  @PostMapping("/test")
  public ResponseEntity<RagQueryResponse> testRag(@Valid @RequestBody RagQueryRequest request) {
    accessGuard.requireProjectAccess(request.projectId());

    RagContext context =
        ragOrchestrator.augmentPrompt(request.projectId(), request.prompt());

    List<ChunkSearchResult> retrieved =
        context.retrievedChunks().stream()
            .map(
                sc ->
                    new ChunkSearchResult(
                        sc.chunk().getId(),
                        sc.chunk().getSourceType(),
                        sc.chunk().getSourceId(),
                        sc.chunk().getChunkText(),
                        sc.chunk().getMetadata(),
                        sc.score()))
            .toList();

    return ResponseEntity.ok(
        new RagQueryResponse(
            context.query(), context.augmentedPrompt(), context.wasAugmented(), retrieved));
  }
}
