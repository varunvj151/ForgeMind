package com.forgemind.modules.ai.tools.impl;

import com.forgemind.modules.ai.tools.AITool;
import com.forgemind.modules.ai.tools.ToolNames;
import com.forgemind.modules.team.dto.response.TeamMemberResponse;
import com.forgemind.modules.team.dto.response.TeamResponse;
import com.forgemind.modules.team.service.TeamService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Fetches a team and its members. Used by sprint-planning to reason about capacity and by
 * assignment suggestions. Wraps {@link TeamService}.
 */
@Component
@RequiredArgsConstructor
public class TeamTool implements AITool<UUID, TeamTool.TeamContext> {

  private static final int MEMBER_LIMIT = 100;

  private final TeamService teamService;

  /** Structured output: the team plus its member list. */
  public record TeamContext(TeamResponse team, List<TeamMemberResponse> members) {}

  @Override
  public String name() {
    return ToolNames.TEAM;
  }

  @Override
  public String description() {
    return "Fetches a team's metadata and member roster for capacity/assignment reasoning.";
  }

  @Override
  public TeamContext execute(UUID teamId) {
    TeamResponse team = teamService.getTeamById(teamId);
    List<TeamMemberResponse> members =
        teamService.listMembers(teamId, Pageable.ofSize(MEMBER_LIMIT)).getContent();
    return new TeamContext(team, members);
  }
}
