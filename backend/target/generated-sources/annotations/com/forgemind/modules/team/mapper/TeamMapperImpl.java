package com.forgemind.modules.team.mapper;

import com.forgemind.modules.auth.dto.UserResponse;
import com.forgemind.modules.team.dto.request.TeamRequest;
import com.forgemind.modules.team.dto.response.TeamMemberResponse;
import com.forgemind.modules.team.dto.response.TeamResponse;
import com.forgemind.modules.team.entity.Team;
import com.forgemind.modules.team.entity.TeamMember;
import com.forgemind.modules.team.entity.TeamRole;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-29T19:18:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class TeamMapperImpl implements TeamMapper {

    @Override
    public TeamResponse toResponse(Team team) {
        if ( team == null ) {
            return null;
        }

        UUID id = null;
        String name = null;
        String description = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = team.getId();
        name = team.getName();
        description = team.getDescription();
        createdAt = team.getCreatedAt();
        updatedAt = team.getUpdatedAt();

        TeamResponse teamResponse = new TeamResponse( id, name, description, createdAt, updatedAt );

        return teamResponse;
    }

    @Override
    public Team toEntity(TeamRequest request) {
        if ( request == null ) {
            return null;
        }

        Team.TeamBuilder team = Team.builder();

        team.name( request.name() );
        team.description( request.description() );

        return team.build();
    }

    @Override
    public void updateEntityFromRequest(TeamRequest request, Team team) {
        if ( request == null ) {
            return;
        }

        team.setName( request.name() );
        team.setDescription( request.description() );
    }

    @Override
    public TeamMemberResponse toResponse(TeamMember member) {
        if ( member == null ) {
            return null;
        }

        UUID teamId = null;
        UserResponse user = null;
        UUID id = null;
        TeamRole role = null;
        Instant joinedAt = null;

        teamId = memberTeamId( member );
        user = toUserResponse( member.getUser() );
        id = member.getId();
        role = member.getRole();
        joinedAt = member.getJoinedAt();

        TeamMemberResponse teamMemberResponse = new TeamMemberResponse( id, teamId, user, role, joinedAt );

        return teamMemberResponse;
    }

    private UUID memberTeamId(TeamMember teamMember) {
        if ( teamMember == null ) {
            return null;
        }
        Team team = teamMember.getTeam();
        if ( team == null ) {
            return null;
        }
        UUID id = team.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
