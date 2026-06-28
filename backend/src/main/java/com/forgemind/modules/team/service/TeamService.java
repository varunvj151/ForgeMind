package com.forgemind.modules.team.service;

import com.forgemind.modules.team.dto.request.AddTeamMemberRequest;
import com.forgemind.modules.team.dto.request.TeamRequest;
import com.forgemind.modules.team.dto.response.TeamMemberResponse;
import com.forgemind.modules.team.dto.response.TeamResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TeamService {

    TeamResponse createTeam(TeamRequest request);

    TeamResponse updateTeam(UUID teamId, TeamRequest request);

    void deleteTeam(UUID teamId);

    TeamResponse getTeamById(UUID teamId);

    Page<TeamResponse> listTeams(Pageable pageable);

    TeamMemberResponse addMember(UUID teamId, AddTeamMemberRequest request);

    void removeMember(UUID teamId, Long userId);

    Page<TeamMemberResponse> listMembers(UUID teamId, Pageable pageable);
}
