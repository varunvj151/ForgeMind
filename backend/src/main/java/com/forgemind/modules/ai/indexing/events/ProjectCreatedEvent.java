package com.forgemind.modules.ai.indexing.events;

import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Published when a new project is created so the indexer can index it. */
@Getter
public class ProjectCreatedEvent extends ApplicationEvent {

  private final UUID projectId;

  public ProjectCreatedEvent(Object source, UUID projectId) {
    super(source);
    this.projectId = projectId;
  }
}
