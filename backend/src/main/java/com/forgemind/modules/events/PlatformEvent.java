package com.forgemind.modules.events;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/** Base class for all domain events in the ForgeMind ecosystem. */
@Getter
public abstract class PlatformEvent {
  
  private final UUID eventId;
  private final UUID organizationId;
  private final String eventType;
  private final Instant timestamp;
  private final String actorId;

  protected PlatformEvent(UUID organizationId, String eventType, String actorId) {
    this.eventId = UUID.randomUUID();
    this.organizationId = organizationId;
    this.eventType = eventType;
    this.actorId = actorId;
    this.timestamp = Instant.now();
  }

  /** Subclasses must return a JSON representation of the event payload. */
  public abstract String getPayloadAsJson();
}
