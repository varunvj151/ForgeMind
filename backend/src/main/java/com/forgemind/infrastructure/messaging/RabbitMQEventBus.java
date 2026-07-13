package com.forgemind.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.events.EventBus;
import com.forgemind.modules.events.PlatformEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * EventBus implementation that publishes platform events to RabbitMQ.
 *
 * <p>Existing {@code @EventListener} handlers on the local Spring context
 * will NOT receive events from this bus directly — they must be registered
 * as {@code @RabbitListener} consumers instead for cross-node delivery.
 * A companion {@link RabbitMQEventRelay} re-publishes incoming AMQP messages
 * as Spring ApplicationEvents for backward compatibility.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.eventbus.provider", havingValue = "rabbitmq")
@RequiredArgsConstructor
public class RabbitMQEventBus implements EventBus {

  public static final String EXCHANGE = "forgemind.events";

  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void publish(PlatformEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      rabbitTemplate.convertAndSend(EXCHANGE, event.getEventType(), payload);
      log.debug("Published event {} to RabbitMQ exchange {}", event.getEventType(), EXCHANGE);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize event {}: {}", event.getClass().getSimpleName(), e.getMessage());
    }
  }
}
