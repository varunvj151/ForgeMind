package com.forgemind.modules.events;

import java.util.UUID;
import lombok.Getter;

@Getter
public class TaskCreatedEvent extends PlatformEvent {
  
  private final UUID taskId;
  private final String taskTitle;
  private final String projectId;

  public TaskCreatedEvent(UUID organizationId, String actorId, UUID taskId, String taskTitle, String projectId) {
    super(organizationId, "task.created", actorId);
    this.taskId = taskId;
    this.taskTitle = taskTitle;
    this.projectId = projectId;
  }

  @Override
  public String getPayloadAsJson() {
    return String.format(
        "{\"taskId\": \"%s\", \"taskTitle\": \"%s\", \"projectId\": \"%s\"}",
        taskId, taskTitle, projectId);
  }
}
