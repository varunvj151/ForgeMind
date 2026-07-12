package com.forgemind.modules.ai.indexing.events;

import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Published when a task is updated so the indexer can re-index it. */
@Getter
public class TaskUpdatedEvent extends ApplicationEvent {

  private final UUID taskId;
  private final UUID projectId;

  public TaskUpdatedEvent(Object source, UUID taskId, UUID projectId) {
    super(source);
    this.taskId = taskId;
    this.projectId = projectId;
  }
}
