package com.forgemind.modules.ai.indexing;

import com.forgemind.modules.ai.indexing.events.ActivityRecordedEvent;
import com.forgemind.modules.ai.indexing.events.DocumentationGeneratedEvent;
import com.forgemind.modules.ai.indexing.events.ProjectCreatedEvent;
import com.forgemind.modules.ai.indexing.events.ProjectDeletedEvent;
import com.forgemind.modules.ai.indexing.events.ProjectUpdatedEvent;
import com.forgemind.modules.ai.indexing.events.TaskCreatedEvent;
import com.forgemind.modules.ai.indexing.events.TaskUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens to domain events and triggers incremental knowledge indexing.
 *
 * <p>All listener methods are {@code @Async} so they never block the caller's transaction.
 * Failures are logged but not propagated — indexing failures must not roll back the domain
 * operation that triggered them.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IndexingEventListener {

  private final KnowledgeIndexer knowledgeIndexer;

  @Async
  @EventListener
  public void onProjectCreated(ProjectCreatedEvent event) {
    log.debug("Indexing newly created project {}", event.getProjectId());
    knowledgeIndexer.indexProject(event.getProjectId());
  }

  @Async
  @EventListener
  public void onProjectUpdated(ProjectUpdatedEvent event) {
    log.debug("Re-indexing updated project {}", event.getProjectId());
    knowledgeIndexer.indexProject(event.getProjectId());
  }

  @Async
  @EventListener
  public void onProjectDeleted(ProjectDeletedEvent event) {
    log.debug("Removing knowledge for deleted project {}", event.getProjectId());
    knowledgeIndexer.deleteProjectKnowledge(event.getProjectId());
  }

  @Async
  @EventListener
  public void onTaskCreated(TaskCreatedEvent event) {
    log.debug("Indexing newly created task {} in project {}", event.getTaskId(), event.getProjectId());
    knowledgeIndexer.indexTask(event.getTaskId(), event.getProjectId());
  }

  @Async
  @EventListener
  public void onTaskUpdated(TaskUpdatedEvent event) {
    log.debug("Re-indexing updated task {} in project {}", event.getTaskId(), event.getProjectId());
    knowledgeIndexer.indexTask(event.getTaskId(), event.getProjectId());
  }

  @Async
  @EventListener
  public void onActivityRecorded(ActivityRecordedEvent event) {
    log.debug("Indexing activity {} in project {}", event.getActivityId(), event.getProjectId());
    knowledgeIndexer.indexActivity(event.getActivityId(), event.getProjectId());
  }

  @Async
  @EventListener
  public void onDocumentationGenerated(DocumentationGeneratedEvent event) {
    log.debug("Indexing documentation '{}' for project {}", event.getTitle(), event.getProjectId());
    knowledgeIndexer.indexDocumentation(event.getProjectId(), event.getTitle(), event.getContent());
  }
}
