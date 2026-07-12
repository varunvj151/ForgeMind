package com.forgemind.modules.ai.knowledge.controller;

import com.forgemind.modules.ai.agent.security.AgentAccessGuard;
import com.forgemind.modules.ai.indexing.KnowledgeIndexer;
import com.forgemind.modules.ai.knowledge.KnowledgeChunkRepository;
import com.forgemind.modules.ai.knowledge.KnowledgeDocumentRepository;
import com.forgemind.modules.ai.knowledge.dto.ChunkSearchResult;
import com.forgemind.modules.ai.knowledge.dto.KnowledgeIndexRequest;
import com.forgemind.modules.ai.knowledge.dto.KnowledgeReindexRequest;
import com.forgemind.modules.ai.knowledge.dto.KnowledgeSearchRequest;
import com.forgemind.modules.ai.knowledge.dto.KnowledgeSearchResponse;
import com.forgemind.modules.ai.knowledge.dto.KnowledgeStatusResponse;
import com.forgemind.modules.ai.retrieval.RetrievalEngine;
import com.forgemind.modules.ai.retrieval.RetrievalQuery;
import com.forgemind.modules.ai.retrieval.RetrievalResult;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for manual inspection and management of the RAG Knowledge Base. */
@RestController
@RequestMapping("/api/v1/ai/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

  private final KnowledgeIndexer indexer;
  private final RetrievalEngine retrievalEngine;
  private final KnowledgeDocumentRepository documentRepo;
  private final KnowledgeChunkRepository chunkRepo;
  private final AgentAccessGuard accessGuard;

  @GetMapping("/projects/{projectId}/status")
  public ResponseEntity<KnowledgeStatusResponse> getStatus(@PathVariable UUID projectId) {
    accessGuard.requireProjectAccess(projectId);
    long docs = documentRepo.countByProjectId(projectId);
    long chunks = chunkRepo.countByProjectId(projectId);
    long pending = chunkRepo.countPendingEmbeddingByProjectId(projectId);
    return ResponseEntity.ok(new KnowledgeStatusResponse(projectId, docs, chunks, pending));
  }

  @PostMapping("/search")
  public ResponseEntity<KnowledgeSearchResponse> search(
      @Valid @RequestBody KnowledgeSearchRequest request) {
    accessGuard.requireProjectAccess(request.projectId());

    RetrievalQuery query =
        RetrievalQuery.builder()
            .projectId(request.projectId())
            .queryText(request.query())
            .sourceType(request.sourceTypeFilter())
            .topK(request.topK() != null ? request.topK() : 5)
            .minScore(request.minScore() != null ? request.minScore() : 0.6)
            .build();

    RetrievalResult result = retrievalEngine.retrieve(query);

    var mappedChunks =
        result.chunks().stream()
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
        new KnowledgeSearchResponse(
            request.query(), result.durationMs(), result.providerName(), mappedChunks));
  }

  @PostMapping("/index/manual")
  public ResponseEntity<Void> indexManual(@Valid @RequestBody KnowledgeIndexRequest request) {
    accessGuard.requireProjectAccess(request.projectId());
    indexer.indexDocumentation(request.projectId(), request.title(), request.content());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/reindex")
  public ResponseEntity<Void> reindexProject(@Valid @RequestBody KnowledgeReindexRequest request) {
    accessGuard.requireProjectAccess(request.projectId());
    indexer.reindexProject(request.projectId());
    return ResponseEntity.ok().build();
  }
}
