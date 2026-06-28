package com.forgemind.modules.activity.mapper;

import com.forgemind.modules.activity.dto.response.ActivityResponse;
import com.forgemind.modules.activity.entity.Activity;
import com.forgemind.modules.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActivityMapper {

    @Mapping(target = "actor", source = "actor")
    ActivityResponse toResponse(Activity activity);

    default ActivityResponse.ActorInfo toActorInfo(User user) {
        if (user == null) {
            return null;
        }
        return new ActivityResponse.ActorInfo(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
