package com.forgemind.modules.activity.service;

import com.forgemind.modules.activity.dto.response.ActivityResponse;
import com.forgemind.modules.activity.entity.ActivityType;
import com.forgemind.modules.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface ActivityService {

    /**
     * Records a new immutable activity event.
     *
     * @param actor      the user who performed the action
     * @param type       the type of activity
     * @param message    human-readable description
     * @param projectId  optional project context
     * @param teamId     optional team context
     * @param taskId     optional task context
     * @param metadata   optional structured key/value pairs (serialized to JSON)
     */
    void record(
            User actor,
            ActivityType type,
            String message,
            UUID projectId,
            UUID teamId,
            UUID taskId,
            Map<String, Object> metadata
    );

    /**
     * Retrieves the activity timeline for a project.
     * Caller must be verified as the project owner before invoking.
     */
    Page<ActivityResponse> getProjectActivities(UUID projectId, Pageable pageable);

    /**
     * Retrieves the activity timeline for a team.
     * Caller must be verified as a team member before invoking.
     */
    Page<ActivityResponse> getTeamActivities(UUID teamId, Pageable pageable);

    /**
     * Retrieves the activity history for the currently authenticated user.
     */
    Page<ActivityResponse> getMyActivities(Pageable pageable);
}
