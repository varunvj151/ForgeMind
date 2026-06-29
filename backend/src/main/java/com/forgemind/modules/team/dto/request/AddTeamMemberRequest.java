package com.forgemind.modules.team.dto.request;

import com.forgemind.modules.team.entity.TeamRole;
import jakarta.validation.constraints.NotNull;

public record AddTeamMemberRequest(
    @NotNull(message = "User ID is required") Long userId,
    @NotNull(message = "Role is required") TeamRole role) {}
