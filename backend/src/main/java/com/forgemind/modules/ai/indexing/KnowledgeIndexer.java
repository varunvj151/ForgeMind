package com.forgemind.modules.ai.indexing;

import java.util.UUID;

/**
 * Coordinates the full knowledge indexing lifecycle.
 *
 * <p>Each method loads the domain entity, chunks it, checks the content checksum, persists new
 * chunks, and asynchronously generates embeddings. Unchanged content (same checksum) is skipped.
 */
public interface KnowledgeIndexer {

  /** Indexes (or re-indexes if changed) a project by its ID. */
  void indexProject(UUID projectId);

  /** Indexes (or re-indexes if changed) a task by its ID. */
  void indexTask(UUID taskId, UUID projectId);

  /** Indexes (or re-indexes if changed) an activity by its ID. */
  void indexActivity(UUID activityId, UUID projectId);

  /**
   * Indexes a documentation artifact (generated markdown) for a project.
   * The {@code title} and {@code content} are provided directly (not loaded from DB).
   */
  void indexDocumentation(UUID projectId, String title, String content);

  /** Fully re-indexes a project: deletes all existing knowledge then rebuilds from scratch. */
  void reindexProject(UUID projectId);

  /** Removes all knowledge documents and chunks for a project (e.g. on project deletion). */
  void deleteProjectKnowledge(UUID projectId);
}
