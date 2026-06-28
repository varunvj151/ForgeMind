package com.forgemind.modules.activity.mapper;

import com.forgemind.modules.activity.dto.response.ActivityResponse;
import com.forgemind.modules.activity.entity.Activity;
import com.forgemind.modules.activity.entity.ActivityType;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-28T21:23:35+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ActivityMapperImpl implements ActivityMapper {

    @Override
    public ActivityResponse toResponse(Activity activity) {
        if ( activity == null ) {
            return null;
        }

        ActivityResponse.ActorInfo actor = null;
        UUID id = null;
        ActivityType activityType = null;
        String message = null;
        String metadata = null;
        UUID projectId = null;
        UUID teamId = null;
        UUID taskId = null;
        Instant createdAt = null;

        actor = toActorInfo( activity.getActor() );
        id = activity.getId();
        activityType = activity.getActivityType();
        message = activity.getMessage();
        metadata = activity.getMetadata();
        projectId = activity.getProjectId();
        teamId = activity.getTeamId();
        taskId = activity.getTaskId();
        createdAt = activity.getCreatedAt();

        ActivityResponse activityResponse = new ActivityResponse( id, actor, activityType, message, metadata, projectId, teamId, taskId, createdAt );

        return activityResponse;
    }
}
