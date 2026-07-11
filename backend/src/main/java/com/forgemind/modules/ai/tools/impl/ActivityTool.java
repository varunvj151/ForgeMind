package com.forgemind.modules.ai.tools.impl;

import com.forgemind.modules.activity.dto.response.ActivityResponse;
import com.forgemind.modules.activity.service.ActivityService;
import com.forgemind.modules.ai.tools.AITool;
import com.forgemind.modules.ai.tools.ToolNames;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Retrieves a project's most recent activity events (newest first). Powers stand-up and risk agents
 * that reason over recent momentum. Wraps {@link ActivityService}; the underlying query is scoped by
 * project and the caller's access is asserted by the agent before this runs.
 */
@Component
@RequiredArgsConstructor
public class ActivityTool implements AITool<ActivityTool.Input, List<ActivityResponse>> {

  private static final int DEFAULT_LIMIT = 50;

  private final ActivityService activityService;

  /** Input: which project and how many recent events to pull. */
  public record Input(UUID projectId, Integer limit) {
    public Input(UUID projectId) {
      this(projectId, DEFAULT_LIMIT);
    }
  }

  @Override
  public String name() {
    return ToolNames.ACTIVITY;
  }

  @Override
  public String description() {
    return "Fetches a project's most recent activity events, newest first.";
  }

  @Override
  public List<ActivityResponse> execute(Input input) {
    int limit = input.limit() != null && input.limit() > 0 ? input.limit() : DEFAULT_LIMIT;
    PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
    return activityService.getProjectActivities(input.projectId(), page).getContent();
  }
}
