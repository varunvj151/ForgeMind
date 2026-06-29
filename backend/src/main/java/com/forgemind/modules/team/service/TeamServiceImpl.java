package com.forgemind.modules.team.service;

import com.forgemind.common.exception.ForgemindException;
import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.modules.activity.entity.ActivityType;
import com.forgemind.modules.activity.service.ActivityService;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.repository.UserRepository;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import com.forgemind.modules.team.dto.request.AddTeamMemberRequest;
import com.forgemind.modules.team.dto.request.TeamRequest;
import com.forgemind.modules.team.dto.response.TeamMemberResponse;
import com.forgemind.modules.team.dto.response.TeamResponse;
import com.forgemind.modules.team.entity.Team;
import com.forgemind.modules.team.entity.TeamMember;
import com.forgemind.modules.team.entity.TeamRole;
import com.forgemind.modules.team.mapper.TeamMapper;
import com.forgemind.modules.team.repository.TeamMemberRepository;
import com.forgemind.modules.team.repository.TeamRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

  private final TeamRepository teamRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final UserRepository userRepository;
  private final TeamMapper teamMapper;
  private final CurrentUserProvider currentUserProvider;
  private final ActivityService activityService;

  @Override
  @Transactional
  public TeamResponse createTeam(TeamRequest request) {
    User currentUser = currentUserProvider.getCurrentUser();

    Team team = teamMapper.toEntity(request);
    Team savedTeam = teamRepository.save(team);

    TeamMember ownerMember =
        TeamMember.builder().team(savedTeam).user(currentUser).role(TeamRole.OWNER).build();

    teamMemberRepository.save(ownerMember);

    log.info(
        "Team created: id={}, name={} by user={}",
        savedTeam.getId(),
        savedTeam.getName(),
        currentUser.getId());
    activityService.record(
        currentUser,
        ActivityType.TEAM_CREATED,
        "Team created",
        null,
        savedTeam.getId(),
        null,
        null);
    return teamMapper.toResponse(savedTeam);
  }

  @Override
  @Transactional
  public TeamResponse updateTeam(UUID teamId, TeamRequest request) {
    Team team = findTeamOrThrow(teamId);
    authorizeRole(teamId, TeamRole.ADMIN, TeamRole.OWNER);

    teamMapper.updateEntityFromRequest(request, team);
    Team saved = teamRepository.save(team);

    log.info("Team updated: id={}", saved.getId());
    return teamMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public void deleteTeam(UUID teamId) {
    Team team = findTeamOrThrow(teamId);
    authorizeRole(teamId, TeamRole.OWNER);

    teamRepository.delete(team);
    log.info("Team deleted: id={}", teamId);
  }

  @Override
  @Transactional(readOnly = true)
  public TeamResponse getTeamById(UUID teamId) {
    Team team = findTeamOrThrow(teamId);
    authorizeRole(teamId, TeamRole.MEMBER, TeamRole.ADMIN, TeamRole.OWNER);
    return teamMapper.toResponse(team);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TeamResponse> listTeams(Pageable pageable) {
    User currentUser = currentUserProvider.getCurrentUser();
    return teamMemberRepository
        .findAllByUserId(currentUser.getId(), pageable)
        .map(member -> teamMapper.toResponse(member.getTeam()));
  }

  @Override
  @Transactional
  public TeamMemberResponse addMember(UUID teamId, AddTeamMemberRequest request) {
    Team team = findTeamOrThrow(teamId);
    authorizeRole(teamId, TeamRole.ADMIN, TeamRole.OWNER);

    if (teamMemberRepository.existsByTeamIdAndUserId(teamId, request.userId())) {
      throw new ForgemindException(
          "DUPLICATE_MEMBER", "User is already a member of this team", HttpStatus.CONFLICT);
    }

    User targetUser =
        userRepository
            .findById(request.userId())
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

    TeamMember newMember =
        TeamMember.builder().team(team).user(targetUser).role(request.role()).build();

    TeamMember saved = teamMemberRepository.save(newMember);
    log.info("Added user={} to team={} with role={}", targetUser.getId(), teamId, request.role());

    activityService.record(
        currentUserProvider.getCurrentUser(),
        ActivityType.TEAM_MEMBER_ADDED,
        "User added to team",
        null,
        teamId,
        null,
        java.util.Map.of("targetUserId", targetUser.getId(), "role", request.role().name()));
    return teamMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public void removeMember(UUID teamId, Long userId) {
    Team team = findTeamOrThrow(teamId);
    authorizeRole(teamId, TeamRole.ADMIN, TeamRole.OWNER);

    TeamMember memberToRemove =
        teamMemberRepository
            .findByTeamIdAndUserId(teamId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "MEMBER_NOT_FOUND", "User is not a member of this team"));

    if (memberToRemove.getRole() == TeamRole.OWNER) {
      throw new ForgemindException(
          "CANNOT_REMOVE_OWNER",
          "Cannot remove an owner from the team. Transfer ownership first.",
          HttpStatus.BAD_REQUEST);
    }

    teamMemberRepository.delete(memberToRemove);
    log.info("Removed user={} from team={}", userId, teamId);
    activityService.record(
        currentUserProvider.getCurrentUser(),
        ActivityType.TEAM_MEMBER_REMOVED,
        "User removed from team",
        null,
        teamId,
        null,
        java.util.Map.of("targetUserId", userId));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TeamMemberResponse> listMembers(UUID teamId, Pageable pageable) {
    findTeamOrThrow(teamId);
    authorizeRole(teamId, TeamRole.MEMBER, TeamRole.ADMIN, TeamRole.OWNER);

    return teamMemberRepository.findAllByTeamId(teamId, pageable).map(teamMapper::toResponse);
  }

  // ── Private Helpers ───────────────────────────────────────────────────────

  private Team findTeamOrThrow(UUID teamId) {
    return teamRepository
        .findById(teamId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "TEAM_NOT_FOUND", "Team not found with id: " + teamId));
  }

  /** Authorizes the current user against a set of allowed roles for the given team. */
  private void authorizeRole(UUID teamId, TeamRole... allowedRoles) {
    User currentUser = currentUserProvider.getCurrentUser();

    TeamMember currentMember =
        teamMemberRepository
            .findByTeamIdAndUserId(teamId, currentUser.getId())
            .orElseThrow(() -> new AccessDeniedException("You are not a member of this team"));

    for (TeamRole allowedRole : allowedRoles) {
      if (currentMember.getRole() == allowedRole) {
        return;
      }
    }

    throw new AccessDeniedException("You do not have the required role to perform this action");
  }
}
