package com.forgemind.modules.ai.tools;

/**
 * Canonical, stable names for every registered {@link AITool}.
 *
 * <p>Referenced by agents when calling {@link ToolExecutor#execute(String, Object)} and by each tool
 * in its {@link AITool#name()}. Keeping them in one place prevents typo-driven "unknown tool"
 * failures and documents the available tool surface at a glance.
 */
public final class ToolNames {

  private ToolNames() {}

  public static final String PROJECT = "project.fetch";
  public static final String TASK = "task.list";
  public static final String ACTIVITY = "activity.recent";
  public static final String TEAM = "team.fetch";
  public static final String METRICS = "metrics.compute";
}
