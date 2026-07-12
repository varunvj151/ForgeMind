package com.forgemind.modules.ai.indexing.events;

import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Published when an activity is recorded so the indexer can index it. */
@Getter
public class ActivityRecordedEvent extends ApplicationEvent {

  private final UUID activityId;
  private final UUID projectId;

  public ActivityRecordedEvent(Object source, UUID activityId, UUID projectId) {
    super(source);
    this.activityId = activityId;
    this.projectId = projectId;
  }
}
