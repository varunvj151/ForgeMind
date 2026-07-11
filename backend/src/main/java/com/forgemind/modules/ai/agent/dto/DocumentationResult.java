package com.forgemind.modules.ai.agent.dto;

/**
 * Structured output of the {@link com.forgemind.modules.ai.agent.DocumentationAgent}.
 *
 * <p>Documentation is inherently prose, so the payload carries the generated markdown along with the
 * kind of document produced (README, ARCHITECTURE, API, RELEASE_NOTES, SPRINT_REPORT, SUMMARY).
 */
public record DocumentationResult(String documentType, String title, String content) {}
