package com.forgemind.modules.ai.indexing;

import com.forgemind.modules.ai.embedding.EmbeddingService;
import com.forgemind.modules.ai.knowledge.KnowledgeChunk;
import com.forgemind.modules.ai.knowledge.KnowledgeDocument;
import com.forgemind.modules.ai.knowledge.KnowledgeChunkRepository;
import com.forgemind.modules.ai.knowledge.KnowledgeDocumentRepository;
import com.forgemind.modules.ai.knowledge.KnowledgeSourceType;
import com.forgemind.modules.activity.dto.response.ActivityResponse;
import com.forgemind.modules.activity.service.ActivityService;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.service.ProjectService;
import com.forgemind.modules.task.dto.response.TaskResponse;
import com.forgemind.modules.task.service.TaskService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default implementation of {@link KnowledgeIndexer}. */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeIndexerImpl implements KnowledgeIndexer {

  private final KnowledgeDocumentRepository documentRepository;
  private final KnowledgeChunkRepository chunkRepository;
  private final KnowledgeChunker knowledgeChunker;
  private final EmbeddingService embeddingService;
  private final ProjectService projectService;
  private final TaskService taskService;
  private final ActivityService activityService;

  @Override
  @Transactional
  public void indexProject(UUID projectId) {
    try {
      ProjectResponse project = projectService.getProjectById(projectId);
      DocumentChunks dc = knowledgeChunker.chunkProject(project);
      persistDocumentChunks(dc);
    } catch (Exception e) {
      log.error("Failed to index project {}: {}", projectId, e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public void indexTask(UUID taskId, UUID projectId) {
    try {
      TaskResponse task = taskService.getTask(taskId);
      DocumentChunks dc = knowledgeChunker.chunkTask(task, projectId);
      persistDocumentChunks(dc);
    } catch (Exception e) {
      log.error("Failed to index task {} for project {}: {}", taskId, projectId, e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public void indexActivity(UUID activityId, UUID projectId) {
    try {
      ActivityResponse activity = activityService.getActivityById(activityId);
      DocumentChunks dc = knowledgeChunker.chunkActivity(activity, projectId);
      persistDocumentChunks(dc);
    } catch (Exception e) {
      log.error("Failed to index activity {} for project {}: {}", activityId, projectId, e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public void indexDocumentation(UUID projectId, String title, String content) {
    try {
      DocumentChunks dc = knowledgeChunker.chunkDocumentation(projectId, title, content);
      persistDocumentChunks(dc);
    } catch (Exception e) {
      log.error("Failed to index documentation for project {}: {}", projectId, e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public void reindexProject(UUID projectId) {
    deleteProjectKnowledge(projectId);
    indexProject(projectId);
    // Also index all tasks
    try {
      List<TaskResponse> tasks = taskService.listProjectTasks(projectId, org.springframework.data.domain.Pageable.unpaged()).getContent();
      for (TaskResponse task : tasks) {
        indexTask(task.id(), projectId);
      }
    } catch (Exception e) {
      log.warn("Partial re-index failure for project {}: {}", projectId, e.getMessage());
    }
  }

  @Override
  @Transactional
  public void deleteProjectKnowledge(UUID projectId) {
    chunkRepository.deleteByProjectId(projectId);
    documentRepository.deleteByProjectId(projectId);
    log.info("Deleted all knowledge for project {}", projectId);
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private void persistDocumentChunks(DocumentChunks dc) {
    if (dc.chunks().isEmpty()) {
      log.debug("Skipping empty DocumentChunks for sourceId={}", dc.document().getSourceId());
      return;
    }

    String newChecksum = computeChecksum(dc.chunks());
    Optional<KnowledgeDocument> existing =
        documentRepository.findBySourceTypeAndSourceId(
            dc.document().getSourceType(), dc.document().getSourceId());

    if (existing.isPresent() && newChecksum.equals(existing.get().getChecksum())) {
      log.debug(
          "Content unchanged for sourceId={} — skipping re-index",
          dc.document().getSourceId());
      return;
    }

    // Delete old chunks if this is an update
    existing.ifPresent(doc -> chunkRepository.deleteByDocumentId(doc.getId()));

    // Save (or update) the document record
    KnowledgeDocument savedDoc;
    if (existing.isPresent()) {
      KnowledgeDocument doc = existing.get();
      doc.setChecksum(newChecksum);
      doc.setChunkCount(dc.chunks().size());
      doc.setIndexedAt(Instant.now());
      doc.setTitle(dc.document().getTitle());
      savedDoc = documentRepository.save(doc);
    } else {
      KnowledgeDocument doc = dc.document();
      doc.setChecksum(newChecksum);
      doc.setChunkCount(dc.chunks().size());
      savedDoc = documentRepository.save(doc);
    }

    // Attach the document ID to each chunk and save
    List<KnowledgeChunk> chunksWithDocId =
        dc.chunks().stream()
            .map(
                c -> {
                  c.setDocumentId(savedDoc.getId());
                  return c;
                })
            .toList();
    List<KnowledgeChunk> savedChunks = chunkRepository.saveAll(chunksWithDocId);

    // Asynchronously generate and persist embeddings
    embeddingService.embedChunks(savedChunks);

    log.info(
        "Indexed {} chunks for sourceId={} ({})",
        savedChunks.size(),
        savedDoc.getSourceId(),
        savedDoc.getSourceType());
  }

  private String computeChecksum(List<KnowledgeChunk> chunks) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      for (KnowledgeChunk chunk : chunks) {
        md.update(chunk.getChunkText().getBytes(StandardCharsets.UTF_8));
      }
      return HexFormat.of().formatHex(md.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
