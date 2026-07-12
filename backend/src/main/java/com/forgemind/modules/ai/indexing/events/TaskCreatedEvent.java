package com.forgemind.modules.ai.indexing.events;

import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Published when a task is created so the indexer can index it. */
@Getter
public class TaskCreatedEvent extends ApplicationEvent {

  private final UUID taskId;
  private final UUID projectId;

  public TaskCreatedEvent(Object source, UUID taskId, UUID projectId) {
    super(source);
    this.taskId = taskId;
    this.projectId = projectId;
  }
}
