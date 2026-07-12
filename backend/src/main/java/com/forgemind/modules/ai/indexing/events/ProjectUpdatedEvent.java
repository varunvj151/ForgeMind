package com.forgemind.modules.ai.indexing.events;

import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Published when a project is updated so the indexer can re-index it. */
@Getter
public class ProjectUpdatedEvent extends ApplicationEvent {

  private final UUID projectId;

  public ProjectUpdatedEvent(Object source, UUID projectId) {
    super(source);
    this.projectId = projectId;
  }
}
