package com.forgemind.modules.ai.indexing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.knowledge.KnowledgeChunk;
import com.forgemind.modules.ai.knowledge.KnowledgeDocument;
import com.forgemind.modules.ai.knowledge.KnowledgeSourceType;
import com.forgemind.modules.activity.dto.response.ActivityResponse;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.task.dto.response.TaskResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Converts ForgeMind domain entities into {@link DocumentChunks} — a parent document record
 * paired with one or more searchable text chunks ready for embedding.
 *
 * <p>Entity data is serialized to a human-readable string that captures the fields most relevant
 * for semantic search. Metadata is stored as a JSON string in the chunk for surfacing in results.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeChunker {

  private final FixedSizeChunker chunker;
  private final ObjectMapper objectMapper;

  // ── Public API ───────────────────────────────────────────────────────────────

  public DocumentChunks chunkProject(ProjectResponse project) {
    String content = buildProjectContent(project);
    KnowledgeDocument doc =
        KnowledgeDocument.builder()
            .sourceType(KnowledgeSourceType.PROJECT)
            .sourceId(project.id().toString())
            .projectId(project.id())
            .title(project.name())
            .build();

    Map<String, Object> meta = new HashMap<>();
    meta.put("sourceType", KnowledgeSourceType.PROJECT.name());
    meta.put("sourceId", project.id().toString());
    meta.put("title", project.name());
    if (project.status() != null) meta.put("status", project.status().name());

    List<KnowledgeChunk> chunks = buildChunks(doc, content, project.id(), meta);
    return new DocumentChunks(doc, chunks);
  }

  public DocumentChunks chunkTask(TaskResponse task, UUID projectId) {
    String content = buildTaskContent(task);
    KnowledgeDocument doc =
        KnowledgeDocument.builder()
            .sourceType(KnowledgeSourceType.TASK)
            .sourceId(task.id().toString())
            .projectId(projectId)
            .title(task.title())
            .build();

    Map<String, Object> meta = new HashMap<>();
    meta.put("sourceType", KnowledgeSourceType.TASK.name());
    meta.put("sourceId", task.id().toString());
    meta.put("title", task.title());
    if (task.status() != null) meta.put("status", task.status().name());
    if (task.priority() != null) meta.put("priority", task.priority().name());

    List<KnowledgeChunk> chunks = buildChunks(doc, content, projectId, meta);
    return new DocumentChunks(doc, chunks);
  }

  public DocumentChunks chunkActivity(ActivityResponse activity, UUID projectId) {
    String content = buildActivityContent(activity);
    KnowledgeDocument doc =
        KnowledgeDocument.builder()
            .sourceType(KnowledgeSourceType.ACTIVITY)
            .sourceId(activity.id().toString())
            .projectId(projectId)
            .title(activity.activityType() + ": " + activity.message())
            .build();

    Map<String, Object> meta = new HashMap<>();
    meta.put("sourceType", KnowledgeSourceType.ACTIVITY.name());
    meta.put("sourceId", activity.id().toString());
    if (activity.activityType() != null) meta.put("activityType", activity.activityType().name());

    List<KnowledgeChunk> chunks = buildChunks(doc, content, projectId, meta);
    return new DocumentChunks(doc, chunks);
  }

  public DocumentChunks chunkDocumentation(UUID projectId, String title, String content) {
    String docSourceId = projectId + ":doc:" + title.replaceAll("\\s+", "_");
    KnowledgeDocument doc =
        KnowledgeDocument.builder()
            .sourceType(KnowledgeSourceType.DOCUMENTATION)
            .sourceId(docSourceId)
            .projectId(projectId)
            .title(title)
            .build();

    Map<String, Object> meta = new HashMap<>();
    meta.put("sourceType", KnowledgeSourceType.DOCUMENTATION.name());
    meta.put("sourceId", docSourceId);
    meta.put("title", title);

    List<KnowledgeChunk> chunks = buildChunks(doc, content, projectId, meta);
    return new DocumentChunks(doc, chunks);
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private List<KnowledgeChunk> buildChunks(
      KnowledgeDocument doc, String content, UUID projectId, Map<String, Object> meta) {
    List<String> texts = chunker.chunk(content, ChunkingOptions.defaults());
    if (texts.isEmpty()) {
      log.debug("No chunks produced for document sourceId={}", doc.getSourceId());
      return List.of();
    }

    String metaJson = toJson(meta);
    List<KnowledgeChunk> chunks = new ArrayList<>(texts.size());
    for (int i = 0; i < texts.size(); i++) {
      chunks.add(
          KnowledgeChunk.builder()
              .projectId(projectId)
              .sourceType(doc.getSourceType())
              .sourceId(doc.getSourceId())
              .chunkIndex(i)
              .chunkText(texts.get(i))
              .metadata(metaJson)
              .createdAt(Instant.now())
              .build());
    }
    return chunks;
  }

  private String buildProjectContent(ProjectResponse p) {
    return "Project: " + p.name()
        + "\nStatus: " + (p.status() != null ? p.status().name() : "UNKNOWN")
        + "\nDescription: " + (p.description() != null ? p.description() : "(no description)");
  }

  private String buildTaskContent(TaskResponse t) {
    StringBuilder sb = new StringBuilder();
    sb.append("Task: ").append(t.title()).append('\n');
    if (t.description() != null) sb.append("Description: ").append(t.description()).append('\n');
    if (t.status() != null) sb.append("Status: ").append(t.status().name()).append('\n');
    if (t.priority() != null) sb.append("Priority: ").append(t.priority().name()).append('\n');
    return sb.toString().strip();
  }

  private String buildActivityContent(ActivityResponse a) {
    StringBuilder sb = new StringBuilder();
    if (a.activityType() != null) sb.append("Type: ").append(a.activityType().name()).append('\n');
    sb.append("Message: ").append(a.message()).append('\n');
    return sb.toString().strip();
  }

  private String toJson(Map<String, Object> map) {
    try {
      return objectMapper.writeValueAsString(map);
    } catch (JsonProcessingException e) {
      log.warn("Failed to serialize chunk metadata: {}", e.getMessage());
      return "{}";
    }
  }
}
