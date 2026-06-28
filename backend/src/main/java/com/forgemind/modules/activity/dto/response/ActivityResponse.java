package com.forgemind.modules.activity.dto.response;

import com.forgemind.modules.activity.entity.ActivityType;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        ActorInfo actor,
        ActivityType activityType,
        String message,
        String metadata,
        UUID projectId,
        UUID teamId,
        UUID taskId,
        Instant createdAt
) {
    /**
     * Minimal actor summary embedded in every activity.
     * Avoids loading the full UserResponse (with roles) for feed queries.
     */
    public record ActorInfo(
            Long id,
            String username,
            String firstName,
            String lastName
    ) {}
}
