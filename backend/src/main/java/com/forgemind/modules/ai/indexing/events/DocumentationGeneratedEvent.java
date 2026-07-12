package com.forgemind.modules.ai.indexing.events;

import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Published when the DocumentationAgent generates content so it can be indexed. */
@Getter
public class DocumentationGeneratedEvent extends ApplicationEvent {

  private final UUID projectId;
  private final String title;
  private final String content;

  public DocumentationGeneratedEvent(Object source, UUID projectId, String title, String content) {
    super(source);
    this.projectId = projectId;
    this.title = title;
    this.content = content;
  }
}
