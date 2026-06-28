package com.forgemind.modules.realtime.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Universal payload for broadcasting real-time events via WebSockets.
 */
public record RealTimeEvent(
        String eventType,
        String entityType,
        UUID entityId,
        Instant timestamp,
        ActorInfo actor,
        Map<String, Object> payload
) {
    public record ActorInfo(Long id, String username, String email) {}
}
