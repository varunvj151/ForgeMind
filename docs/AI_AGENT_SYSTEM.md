# ForgeMind — Phase 6: AI Agent System

This document describes the AI Agent platform layered on top of the existing AI module. It is
**additive**: no existing AI provider, prompt, context or orchestrator API was changed, and the
original `/api/v1/ai/projects/**` endpoints continue to work unchanged.

---

## 1. Architecture Overview

The agent system turns ForgeMind from "an app that calls an LLM" into an **extensible agent
platform**. It introduces four cooperating layers, all built on the existing AI primitives
(`AiProvider`, `PromptTemplateManager`, `AiContextBuilder`):

```
                    ┌─────────────────────────────────────────────┐
   HTTP  ─────────► │  AgentController  (/api/v1/ai/agents/**)     │
                    └───────────────┬─────────────────────────────┘
                                    │ capability + AgentRequest
                    ┌───────────────▼─────────────────────────────┐
                    │  AgentService → AgentRegistry (routing)      │
                    └───────────────┬─────────────────────────────┘
                                    │ resolve(capability)
             ┌──────────────────────▼───────────────────────────────┐
             │                 Agent (interface)                     │
             │   AbstractAgent template:                             │
             │   validate → authorize → gather(tools) → prompt →     │
             │   provider → structured payload → telemetry           │
             └───┬─────────────┬──────────────┬───────────┬──────────┘
                 │             │              │           │
        ┌────────▼──┐  ┌───────▼──────┐  ┌────▼─────┐ ┌───▼────────────┐
        │ ToolExecutor │ AgentAccess  │  │ Prompt   │ │  AiProvider     │
        │  + AITools │  │ Guard (sec.) │  │ Template │ │ (Gemini / Mock) │
        └────┬───────┘  └──────────────┘  │ Manager  │ └─────────────────┘
             │                            └──────────┘
   ┌─────────▼──────────────────────────────────┐
   │ ProjectTool TaskTool ActivityTool           │  → existing domain services
   │ TeamTool MetricsTool                         │    (authenticated, DTO-only)
   └──────────────────────────────────────────────┘

   WorkflowEngine ── runs ordered multi-agent Workflows over the AgentRegistry
   AgentMemory    ── short-term, per-execution scratchpad
   AgentMetrics   ── Micrometer telemetry (ai.agent.*)
```

**Key idea:** business code depends only on the `Agent` interface (resolved by `AgentCapability`
through the `AgentRegistry`). Adding an agent = implement `Agent` + declare a capability + register
it as a Spring bean. No orchestration code changes — the registry and workflow engine never learn
about specific agents.

---

## 2. Folder Structure

```
modules/ai/
├── agent/
│   ├── Agent.java                    # core interface
│   ├── AbstractAgent.java            # template-method base (validate→auth→reason→telemetry)
│   ├── AgentCapability.java          # capability enum (routing key)
│   ├── AgentRegistry.java            # capability → agent resolver (no if/else chains)
│   ├── AgentService.java             # application-facing façade
│   ├── AgentServiceImpl.java
│   ├── AgentController.java          # REST surface /api/v1/ai/agents/**
│   ├── AgentPrompts.java             # agent-owned prompt templates
│   ├── PlannerAgent.java
│   ├── DocumentationAgent.java
│   ├── RiskAnalysisAgent.java
│   ├── SprintPlanningAgent.java
│   ├── StandupAgent.java
│   ├── dto/
│   │   ├── AgentRequest.java         # uniform input
│   │   ├── AgentResponse.java        # uniform envelope (payload + telemetry)
│   │   ├── PlanResult.java
│   │   ├── RiskAnalysisResult.java
│   │   ├── SprintPlanResult.java
│   │   ├── StandupResult.java
│   │   └── DocumentationResult.java
│   └── security/
│       └── AgentAccessGuard.java     # per-run authorization checkpoint
├── tools/
│   ├── AITool.java                   # tool interface
│   ├── ToolExecutor.java             # registry + single execution entry point
│   ├── ToolNames.java                # canonical tool names
│   ├── ToolExecutionException.java
│   └── impl/
│       ├── ProjectTool.java
│       ├── TaskTool.java
│       ├── ActivityTool.java
│       ├── TeamTool.java
│       └── MetricsTool.java          # deterministic metrics (no LLM)
├── memory/
│   └── AgentMemory.java              # short-term execution memory
├── workflow/
│   ├── Workflow.java                 # ordered steps (pure data)
│   ├── WorkflowStep.java
│   ├── WorkflowContext.java          # threads results between steps
│   ├── WorkflowResult.java
│   └── WorkflowEngine.java           # deterministic orchestrator
├── observability/
│   └── AgentMetrics.java             # Micrometer ai.agent.* meters
└── dto/request/
    ├── AgentPlanRequest.java
    └── AgentSprintRequest.java
```

---

## 3. Files Created

**Agent core:** `Agent`, `AbstractAgent`, `AgentCapability`, `AgentRegistry`, `AgentService`,
`AgentServiceImpl`, `AgentController`, `AgentPrompts`.

**Agents:** `PlannerAgent`, `DocumentationAgent`, `RiskAnalysisAgent`, `SprintPlanningAgent`,
`StandupAgent`.

**Agent DTOs:** `AgentRequest`, `AgentResponse`, `PlanResult`, `RiskAnalysisResult`,
`SprintPlanResult`, `StandupResult`, `DocumentationResult`.

**Security:** `AgentAccessGuard`.

**Tools:** `AITool`, `ToolExecutor`, `ToolNames`, `ToolExecutionException`, `ProjectTool`,
`TaskTool`, `ActivityTool`, `TeamTool`, `MetricsTool`.

**Memory:** `AgentMemory`.

**Workflow:** `Workflow`, `WorkflowStep`, `WorkflowContext`, `WorkflowResult`, `WorkflowEngine`.

**Observability:** `AgentMetrics`.

**Request DTOs:** `AgentPlanRequest`, `AgentSprintRequest`.

**Tests:** `AgentRegistryTest`, `ToolExecutorTest`, `MetricsToolTest`, `AgentMemoryTest`,
`PlannerAgentTest`, `RiskAnalysisAgentTest`, `WorkflowEngineTest`.

## 4. Files Modified

- `src/test/.../ai/context/AiContextBuilderTest.java` — updated to construct `ProjectResponse` /
  `TaskResponse` via their record constructors (the test predated their conversion to records and
  no longer compiled against the current DTOs). **No production file was modified** — the agent
  system is purely additive.

---

## 5. Agent Interface Design

```java
public interface Agent<T> {
    AgentCapability capability();          // routing identity
    AgentResponse<T> execute(AgentRequest request);
}
```

- **Uniform I/O:** every agent takes an `AgentRequest` (projectId + free-form `parameters` map) and
  returns an `AgentResponse<T>` (structured `payload` + provider/model/token/duration telemetry).
- **Single responsibility:** one capability per agent.
- **`AbstractAgent`** fixes the lifecycle as a template method so no agent can skip steps:
  1. `validate(request)` — fail fast on bad input.
  2. `AgentAccessGuard.requireProjectAccess(projectId)` — authorization.
  3. `doExecute(request, memory)` — gather context via **tools**, build a prompt via
     `PromptTemplateManager`, call `AiProvider`, parse into a structured DTO.
  4. wrap payload + record success/failure metrics.
- Subclasses only implement `capability()`, optionally `validate()`, and `doExecute()`.

Adding an agent requires **zero** changes to the registry, engine, controller-routing, or other
agents — satisfying the open/closed principle.

---

## 6. Tool Framework Architecture

```java
public interface AITool<I, O> {
    String name();
    String description();
    O execute(I input);          // structured output, never raw text
}
```

- Tools are the **only** way agents reach the domain. Agents never inject repositories or domain
  services directly — they compose tools through the `ToolExecutor`.
- `ToolExecutor` auto-discovers all `AITool` beans, indexes them by `name()` (duplicate names fail
  fast at startup), validates existence on call, records per-tool metrics, and wraps failures in
  `ToolExecutionException`.
- Every tool delegates to an **existing authenticated domain service** (`ProjectService`,
  `TaskService`, `ActivityService`, `TeamService`), so tool calls inherit the platform's normal
  authorization and DTO boundaries.
- `MetricsTool` is a pure, deterministic computation over an already-fetched task list — it gives
  the LLM hard ground-truth numbers (overdue, blocked, completion ratio, distributions) instead of
  asking it to count.

---

## 7. Workflow Engine Design

```java
Workflow = ordered List<WorkflowStep>          // pure data
WorkflowStep = (name, capability, requestFactory(context))
WorkflowEngine.execute(workflow) → WorkflowResult
```

- The engine iterates steps in order: resolve the agent by capability from the registry, build the
  step's `AgentRequest` (the factory can read earlier results from `WorkflowContext`), execute, and
  record the response back into the context for later steps.
- `WorkflowContext` is the multi-step analogue of `AgentMemory`: it threads step N-1's output into
  step N's input, enabling chains like **Planner → Documentation** ("plan a feature, then document
  the plan").
- The engine depends only on `AgentRegistry` + `Agent` — it knows nothing about concrete agents or
  tools, so new multi-agent flows are declared as data without touching orchestration code.
- Each agent still runs its own validation + authorization, so orchestration cannot bypass security.

Example flow realised by the design (`Planner → Project/Task/Activity tools → Prompt → Provider →
structured response`) is exactly what `PlannerAgent.doExecute` performs; multi-agent chains compose
these via `WorkflowEngine`.

---

## 8. Security Considerations

- **No authorization bypass.** Every agent, before gathering any data, calls
  `AgentAccessGuard.requireProjectAccess(projectId)`, which reuses the existing `CurrentUserProvider`
  and the project ownership model. A user who does not own the project receives `AccessDeniedException`
  (HTTP 403 via the global handler) and no project data is ever fetched or returned.
- **Access checked once, up front** — not left to each tool to remember.
- **Tools use authenticated domain services**, never raw repositories, so the existing access rules
  and DTO boundaries apply to every tool call.
- **All `/api/v1/ai/agents/**` routes require authentication** (global security config; only the
  documented public paths are open).
- **No sensitive data in telemetry/logs.** Metrics record only metadata (capability, provider, tool
  names, durations, counts) — never prompt text or project content. Failure metrics record the
  exception *type*, not its message.

---

## 9. Metrics Collected (Micrometer, `ai.agent.*`)

| Metric | Type | Tags | Meaning |
|---|---|---|---|
| `ai.agent.execution.duration` | Timer | `capability`, `provider`, `outcome` | Agent wall-clock time |
| `ai.agent.execution.success` | Counter | `capability`, `provider` | Successful runs |
| `ai.agent.execution.failure` | Counter | `capability`, `reason` | Failed runs (reason = exception class) |
| `ai.agent.execution.retries` | Counter | `capability` | Retries |
| `ai.agent.tool.duration` | Timer | `tool` | Per-tool execution time |
| `ai.agent.tool.executions` | Counter | `tool` | Tool invocation count |

These complement the pre-existing `ai.provider.*` meters emitted by the providers, and are exposed
through the existing Actuator/Prometheus wiring. Captured dimensions: **agent invoked, execution
time, provider used, tools executed, failures, retries** — all metadata-only.

---

## 10. Testing Strategy

All tests are pure unit / mock tests — **no test calls an external LLM** (the `AiProvider` is always
stubbed or `MockAiProvider`).

- **`AgentRegistryTest`** — routing, `supports`, unknown-capability failure, duplicate-capability
  fail-fast.
- **`ToolExecutorTest`** — registration, lookup, unknown-tool + failure wrapping, duplicate-name
  fail-fast.
- **`MetricsToolTest`** — deterministic metric math (overdue/blocked/completion/distributions).
- **`AgentMemoryTest`** — short-term memory round-trip and tool-result namespacing.
- **`PlannerAgentTest`** — validation, context reuse, structured plan parsing (stubbed provider).
- **`RiskAnalysisAgentTest`** — end-to-end tool composition + authorization (owner allowed, non-owner
  denied) + structured JSON parsing.
- **`WorkflowEngineTest`** — ordered execution, result aggregation, context threading between steps.

Recommended additions for CI: a `@WebMvcTest` slice for `AgentController` and an integration test
using the `MOCK` provider profile end-to-end.

---

## 11. Future Agent Extensions

The platform is designed so each of these is "implement `Agent`, add a capability, register the
bean, reuse existing tools":

- **Code Review Agent** — reason over PR/commit metadata (needs a new `VcsTool`).
- **Retrospective Agent** — synthesise sprint retrospectives from activity + completed sprint plans.
- **Estimation / Effort Agent** — historical-velocity-based effort prediction (reuses `MetricsTool`).
- **Dependency / Blocker Resolver** — chain after `RiskAnalysisAgent` in a workflow to propose
  unblocking actions.
- **Onboarding Agent** — generate a "getting started" guide from project + docs.
- **Notification / Digest Agent** — periodic stand-up/risk digests via a scheduled workflow.

New tools (e.g. `VcsTool`, `SearchTool`, `DocumentationStoreTool`) plug into the same `ToolExecutor`
and become instantly available to every agent.

Persistent, cross-session agent memory is intentionally **out of scope for this phase** (only
short-term `AgentMemory` is implemented) and is the natural next step.
```
