package com.forgemind.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.events.EventBus;
import com.forgemind.modules.events.PlatformEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * EventBus implementation that publishes platform events to Kafka.
 * Topic naming convention: forgemind.{eventType} (dots replaced with hyphens for Kafka).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.eventbus.provider", havingValue = "kafka")
@RequiredArgsConstructor
public class KafkaEventBus implements EventBus {

  private static final String TOPIC_PREFIX = "forgemind.";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void publish(PlatformEvent event) {
    try {
      String topic = TOPIC_PREFIX + event.getEventType().replace(".", "-");
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(topic, event.getOrganizationId().toString(), payload)
          .whenComplete((result, ex) -> {
            if (ex != null) {
              log.error("Kafka send failed for event {}: {}", event.getEventType(), ex.getMessage());
            } else {
              log.debug("Published event {} to Kafka topic {}", event.getEventType(), topic);
            }
          });
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize event {}: {}", event.getClass().getSimpleName(), e.getMessage());
    }
  }
}
