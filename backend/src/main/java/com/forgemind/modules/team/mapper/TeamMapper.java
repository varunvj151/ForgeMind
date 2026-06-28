package com.forgemind.modules.team.mapper;

import com.forgemind.modules.auth.dto.UserResponse;
import com.forgemind.modules.team.dto.request.TeamRequest;
import com.forgemind.modules.team.dto.response.TeamMemberResponse;
import com.forgemind.modules.team.dto.response.TeamResponse;
import com.forgemind.modules.team.entity.Team;
import com.forgemind.modules.team.entity.TeamMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMapper {

    TeamResponse toResponse(Team team);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "members", ignore = true)
    Team toEntity(TeamRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "members", ignore = true)
    void updateEntityFromRequest(TeamRequest request, @MappingTarget Team team);

    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "user", source = "user")
    TeamMemberResponse toResponse(TeamMember member);

    default UserResponse toUserResponse(com.forgemind.modules.auth.entity.User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.fromUser(user);
    }
}
