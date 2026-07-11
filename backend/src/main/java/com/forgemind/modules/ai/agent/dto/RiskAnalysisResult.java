package com.forgemind.modules.ai.agent.dto;

import java.util.List;

/**
 * Structured output of the {@link com.forgemind.modules.ai.agent.RiskAnalysisAgent}.
 *
 * <p>Combines a deterministic overall risk score with the LLM's qualitative reasoning, reasons and
 * recommended next actions.
 */
public record RiskAnalysisResult(
    int riskScore,
    String riskLevel,
    String summary,
    List<RiskItem> risks,
    List<String> recommendations,
    List<String> nextActions) {

  /** A single identified risk with its severity and rationale. */
  public record RiskItem(String level, String area, String description, String recommendation) {}
}
