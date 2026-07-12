package com.forgemind.modules.events;

import java.util.UUID;
import lombok.Getter;

@Getter
public class ProjectCreatedEvent extends PlatformEvent {
  
  private final UUID projectId;
  private final String projectName;

  public ProjectCreatedEvent(UUID organizationId, String actorId, UUID projectId, String projectName) {
    super(organizationId, "project.created", actorId);
    this.projectId = projectId;
    this.projectName = projectName;
  }

  @Override
  public String getPayloadAsJson() {
    return String.format(
        "{\"projectId\": \"%s\", \"projectName\": \"%s\"}",
        projectId, projectName);
  }
}
