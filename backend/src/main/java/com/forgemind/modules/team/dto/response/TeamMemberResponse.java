package com.forgemind.modules.team.dto.response;

import com.forgemind.modules.auth.dto.UserResponse;
import com.forgemind.modules.team.entity.TeamRole;
import java.time.Instant;
import java.util.UUID;

public record TeamMemberResponse(
    UUID id, UUID teamId, UserResponse user, TeamRole role, Instant joinedAt) {}
