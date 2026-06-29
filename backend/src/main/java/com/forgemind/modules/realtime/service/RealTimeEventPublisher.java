package com.forgemind.modules.realtime.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.activity.entity.Activity;
import com.forgemind.modules.realtime.dto.RealTimeEvent;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealTimeEventPublisher {

  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;

  /**
   * Converts an Activity record into a RealTimeEvent and broadcasts it to relevant topics.
   *
   * @param activity The saved activity event
   */
  public void publish(Activity activity) {
    try {
      Map<String, Object> payload = extractPayload(activity.getMetadata());
      RealTimeEvent.ActorInfo actorInfo =
          new RealTimeEvent.ActorInfo(
              activity.getActor().getId(),
              activity.getActor().getUsername(),
              activity.getActor().getEmail());

      // Determine the primary entity type and ID based on the activity type
      String eventType = activity.getActivityType().name();
      String entityType = deriveEntityType(eventType);
      UUID entityId = deriveEntityId(activity, entityType);

      RealTimeEvent event =
          new RealTimeEvent(
              eventType, entityType, entityId, activity.getCreatedAt(), actorInfo, payload);

      broadcastToTopics(activity, event);

    } catch (Exception e) {
      log.error(
          "Failed to broadcast real-time event for activity {}: {}",
          activity.getId(),
          e.getMessage());
    }
  }

  private void broadcastToTopics(Activity activity, RealTimeEvent event) {
    // 1. Broadcast to dedicated Activity feed if related to a project
    if (activity.getProjectId() != null) {
      String activityTopic = "/topic/activity/" + activity.getProjectId();
      messagingTemplate.convertAndSend(activityTopic, event);
      log.debug("Published event {} to topic {}", event.eventType(), activityTopic);
    }

    // 2. Broadcast to specific entity topics
    String eventType = event.eventType();

    if (eventType.startsWith("PROJECT_") && activity.getProjectId() != null) {
      String topic = "/topic/projects/" + activity.getProjectId();
      messagingTemplate.convertAndSend(topic, event);
      log.debug("Published event {} to topic {}", event.eventType(), topic);
    } else if (eventType.startsWith("TASK_") && activity.getProjectId() != null) {
      String topic = "/topic/tasks/" + activity.getProjectId();
      messagingTemplate.convertAndSend(topic, event);
      log.debug("Published event {} to topic {}", event.eventType(), topic);
    } else if (eventType.startsWith("TEAM_") && activity.getTeamId() != null) {
      String topic = "/topic/teams/" + activity.getTeamId();
      messagingTemplate.convertAndSend(topic, event);
      log.debug("Published event {} to topic {}", event.eventType(), topic);
    }
  }

  private String deriveEntityType(String eventType) {
    if (eventType.startsWith("PROJECT_")) return "PROJECT";
    if (eventType.startsWith("TASK_")) return "TASK";
    if (eventType.startsWith("TEAM_")) return "TEAM";
    return "UNKNOWN";
  }

  private UUID deriveEntityId(Activity activity, String entityType) {
    switch (entityType) {
      case "PROJECT":
        return activity.getProjectId();
      case "TASK":
        return activity.getTaskId();
      case "TEAM":
        return activity.getTeamId();
      default:
        return null;
    }
  }

  private Map<String, Object> extractPayload(String metadataJson) {
    if (metadataJson == null || metadataJson.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException e) {
      log.warn("Failed to parse metadata JSON for real-time payload: {}", e.getMessage());
      return null;
    }
  }
}
