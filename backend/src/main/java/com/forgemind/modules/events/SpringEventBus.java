package com.forgemind.modules.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringEventBus implements EventBus {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void publish(PlatformEvent event) {
    log.debug("Publishing event: {}", event.getClass().getSimpleName());
    applicationEventPublisher.publishEvent(event);
  }
}
