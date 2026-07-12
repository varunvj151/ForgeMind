package com.forgemind.modules.webhooks.service;

import com.forgemind.modules.events.PlatformEvent;
import com.forgemind.modules.webhooks.entity.WebhookEndpoint;
import com.forgemind.modules.webhooks.repository.WebhookEndpointRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Listens to internal Spring ApplicationEvents and triggers webhooks. */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookEventListener {

  private final WebhookEndpointRepository endpointRepository;
  private final WebhookDispatcher dispatcher;

  @Async
  @EventListener
  public void onPlatformEvent(PlatformEvent event) {
    if (event.getOrganizationId() == null) {
      return; // Global events don't trigger tenant webhooks
    }

    log.debug("Received platform event for webhooks: {}", event.getEventType());
    
    // Find all active endpoints subscribed to this event type
    List<WebhookEndpoint> endpoints = endpointRepository.findActiveEndpointsForEvent(
        event.getOrganizationId(), event.getEventType());

    if (endpoints.isEmpty()) {
      return;
    }

    String payload = event.getPayloadAsJson();
    
    for (WebhookEndpoint endpoint : endpoints) {
      log.debug("Dispatching event {} to webhook {}", event.getEventType(), endpoint.getId());
      dispatcher.dispatch(endpoint, event.getEventType(), event.getEventId(), payload);
    }
  }
}
