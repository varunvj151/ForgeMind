package com.forgemind.modules.ai.agent;

/**
 * Enumerates the discrete capabilities that agents in the platform can fulfil.
 *
 * <p>A capability is the stable, machine-routable identity of an agent's responsibility. Business
 * services request work by capability — never by concrete agent class — so new agents can be
 * introduced (or swapped) without touching call sites. The {@link AgentRegistry} maps exactly one
 * {@link Agent} to each capability.
 *
 * <p>Add a new constant here only when introducing a genuinely new agent responsibility. Never
 * rename or remove existing constants — doing so would break persisted workflow definitions and API
 * contracts.
 */
public enum AgentCapability {

  /** Break a feature description into milestones, phases, tasks, dependencies and estimates. */
  PLANNING,

  /** Generate project documentation (README, architecture, API docs, release notes, summaries). */
  DOCUMENTATION,

  /** Analyse project health and surface risks, reasons and recommended actions. */
  RISK_ANALYSIS,

  /** Produce a sprint backlog with ordering, dependencies and suggested assignments. */
  SPRINT_PLANNING,

  /** Generate a stand-up report (yesterday / today / blockers / upcoming) from activity history. */
  STANDUP
}
