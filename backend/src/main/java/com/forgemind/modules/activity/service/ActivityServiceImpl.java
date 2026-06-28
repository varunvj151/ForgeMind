package com.forgemind.modules.activity.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.activity.dto.response.ActivityResponse;
import com.forgemind.modules.activity.entity.Activity;
import com.forgemind.modules.activity.entity.ActivityType;
import com.forgemind.modules.activity.mapper.ActivityMapper;
import com.forgemind.modules.activity.repository.ActivityRepository;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import com.forgemind.modules.project.repository.ProjectRepository;
import com.forgemind.modules.realtime.service.RealTimeEventPublisher;
import com.forgemind.modules.team.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final CurrentUserProvider currentUserProvider;
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ObjectMapper objectMapper;
    private final RealTimeEventPublisher realTimeEventPublisher;

    // ── Record ────────────────────────────────────────────────────────────────

    /**
     * Records an activity within the CURRENT transaction.
     * Uses {@link Propagation#REQUIRED} so it participates in the caller's
     * transaction — if the main operation rolls back, the activity is also
     * rolled back, maintaining consistency.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void record(
            User actor,
            ActivityType type,
            String message,
            UUID projectId,
            UUID teamId,
            UUID taskId,
            Map<String, Object> metadata) {

        String metadataJson = null;
        if (metadata != null && !metadata.isEmpty()) {
            try {
                metadataJson = objectMapper.writeValueAsString(metadata);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize activity metadata for type={}: {}", type, e.getMessage());
                // Do not fail the main business operation due to metadata serialization
            }
        }

        Activity activity = Activity.builder()
                .actor(actor)
                .activityType(type)
                .message(message)
                .metadata(metadataJson)
                .projectId(projectId)
                .teamId(teamId)
                .taskId(taskId)
                .build();

        activityRepository.save(activity);
        log.debug("Activity recorded: type={}, actor={}, project={}, team={}, task={}",
                type, actor.getId(), projectId, teamId, taskId);

        // Broadcast real-time event
        realTimeEventPublisher.publish(activity);
    }

    // ── Project timeline ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityResponse> getProjectActivities(UUID projectId, Pageable pageable) {
        User currentUser = currentUserProvider.getCurrentUser();

        // Authorization: only the project owner may view its activity feed
        projectRepository.findById(projectId).ifPresent(project -> {
            if (!project.getOwner().getId().equals(currentUser.getId())) {
                log.warn("Unauthorized project activity access: user={}, project={}", currentUser.getId(), projectId);
                throw new AccessDeniedException("You do not have permission to view this project's activity feed.");
            }
        });

        return activityRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(activityMapper::toResponse);
    }

    // ── Team timeline ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityResponse> getTeamActivities(UUID teamId, Pageable pageable) {
        User currentUser = currentUserProvider.getCurrentUser();

        // Authorization: only team members may view the team activity feed
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, currentUser.getId())) {
            log.warn("Unauthorized team activity access: user={}, team={}", currentUser.getId(), teamId);
            throw new AccessDeniedException("You must be a member of this team to view its activity feed.");
        }

        return activityRepository.findByTeamIdOrderByCreatedAtDesc(teamId, pageable)
                .map(activityMapper::toResponse);
    }

    // ── User activity ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityResponse> getMyActivities(Pageable pageable) {
        User currentUser = currentUserProvider.getCurrentUser();
        return activityRepository.findByActorIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(activityMapper::toResponse);
    }
}
