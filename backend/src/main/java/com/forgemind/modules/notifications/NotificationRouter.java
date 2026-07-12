package com.forgemind.modules.notifications;

import com.forgemind.modules.events.PlatformEvent;
import com.forgemind.modules.organization.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Routes platform events to the appropriate notification channels. */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRouter {

  private final EmailService emailService;

  @Async
  @EventListener
  public void onPlatformEvent(PlatformEvent event) {
    log.debug("Routing notification for event: {}", event.getEventType());
    
    // In a real implementation, this would look up user notification preferences
    // and route to Email, Slack, In-App, etc.
    
    if (event.getEventType().equals("task.created")) {
      // Simulate sending an email notification
      emailService.sendNotification(
          "user@example.com", 
          "New Task Created", 
          "A new task was created: " + event.getPayloadAsJson()
      );
    }
  }
}
