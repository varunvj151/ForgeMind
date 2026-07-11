package com.forgemind.modules.ai.tools;

/**
 * A single, composable capability that an agent can invoke to gather data or perform a scoped action
 * against the domain.
 *
 * <p>Tools are the <strong>only</strong> way agents touch the rest of the system. An agent never
 * injects a repository or a domain service directly; it composes tools. This keeps agents focused on
 * reasoning (which context to gather, how to prompt) while tools own the "how" of talking to the
 * domain — and, critically, every tool call flows through the existing authenticated domain services
 * so authorization is never bypassed.
 *
 * <p>Each tool has exactly one responsibility. Implementations are Spring beans and are discovered
 * automatically by the {@link ToolExecutor}; agents resolve them by {@link #name()} through the
 * executor rather than instantiating them.
 *
 * @param <I> the tool's typed input
 * @param <O> the tool's typed, structured output
 */
public interface AITool<I, O> {

  /** Stable, unique identifier used to register and look up the tool. */
  String name();

  /** Human-readable description of what the tool does (useful for logging and future tool-choice). */
  String description();

  /**
   * Executes the tool's single responsibility.
   *
   * @param input typed input for this tool (may be {@code null} for no-arg tools)
   * @return structured output; never raw, unparsed text
   */
  O execute(I input);
}
