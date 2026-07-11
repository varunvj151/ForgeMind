package com.forgemind.modules.ai.agent.dto;

import java.util.List;

/**
 * Structured output of the {@link com.forgemind.modules.ai.agent.StandupAgent}.
 *
 * <p>The classic stand-up shape, derived from recent activity and task changes.
 */
public record StandupResult(
    List<String> yesterday,
    List<String> today,
    List<String> blockers,
    List<String> upcomingPriorities) {}
