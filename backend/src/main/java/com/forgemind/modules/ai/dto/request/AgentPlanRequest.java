package com.forgemind.modules.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Inbound payload for the planner agent endpoint. */
@Data
public class AgentPlanRequest {

  @NotBlank(message = "Feature description is required")
  private String featureDescription;
}
