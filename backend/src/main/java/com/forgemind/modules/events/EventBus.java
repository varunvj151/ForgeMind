package com.forgemind.modules.events;

/**
 * An abstraction for the internal event-driven architecture.
 *
 * <p>Business services should depend on this interface to publish domain events
 * rather than coupling directly to Spring's ApplicationEventPublisher or a specific
 * message broker (like RabbitMQ or Kafka).
 */
public interface EventBus {
  
  /** Publishes an event to all subscribed listeners. */
  void publish(PlatformEvent event);
}
