package com.forgemind.modules.ai.indexing.events;

import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Published when a project is deleted so the indexer can remove its knowledge. */
@Getter
public class ProjectDeletedEvent extends ApplicationEvent {

  private final UUID projectId;

  public ProjectDeletedEvent(Object source, UUID projectId) {
    super(source);
    this.projectId = projectId;
  }
}
