package com.forgemind.modules.ai.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Inbound payload for the sprint-planning agent endpoint. */
@Data
public class AgentSprintRequest {

  @NotNull(message = "Sprint duration (days) is required")
  @Min(value = 1, message = "Sprint duration must be at least 1 day")
  private Integer sprintDurationDays;

  @NotNull(message = "Team size is required")
  @Min(value = 1, message = "Team size must be at least 1")
  private Integer teamSize;
}
