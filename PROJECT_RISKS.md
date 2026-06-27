# ForgeMind — Project Risks, Contradictions, and Missing Requirements

As Lead Software Architect for ForgeMind, this document details the key risks, logical contradictions, missing requirements, and recommended improvements identified during the architecture review of the `09-48` documentation set.

---

## 1. Missing Requirements

### MR-001: Missing Baseline Documentation (01–08)
- **Description:** The documentation set refers extensively to baseline documents `01-requirements.md` through `08-memory.md` (e.g., `02-architecture.md`, `03-database.md`, `04-api-design.md`, `07-ai-orchestration.md`, `08-memory.md`). However, these files do not exist in the repository.
- **Impact:** Critical details about the REST API specs, the initial database schema (referred to in `12-er-diagrams.md`), and the core AI Orchestrator's internal interfaces are missing and must be inferred.

### MR-002: Sandbox / Isolation for Code Execution & Self-Healing
- **Description:** During the "Self-Healing" phase (Stage 7 of the Project Generation Pipeline) and general workspace builds, the backend must compile and run generated code (including unit tests). There is no requirement or specification for isolating these build environments in a secure sandbox (e.g., gVisor, Firecracker microVMs) for Phase 0 (MVP).
- **Impact:** Critical security vulnerability. If the AI generates code containing malicious system calls (e.g., deleting files, spawning reverse shells) or enters an infinite loop, it can crash or compromise the host backend system.

### MR-003: User-Level Token Quota & Cost Tracking
- **Description:** While the `generations` table tracks `tokens_used`, there are no requirements, API endpoints, or database fields defined for enforcing token limits, budget caps, or user quotas.
- **Impact:** Operational cost risk. A single user could run infinite generation/self-healing loops and incur massive API bills with no built-in mechanism to throttle them.

### MR-004: Workspace File Concurrency and Locking
- **Description:** The platform supports an AI chat panel that can perform surgical edits on workspace files while the user is actively using the Monaco Editor. There is no specification for concurrency control, conflict resolution (e.g., Operational Transformation/CRDTs), or simple file-locking.
- **Impact:** Race conditions and data loss. Simultaneous edits by the user and the AI Agent will overwrite each other.

---

## 2. Contradictions & Ambiguities

### C-001: WebSocket Broker Horizontal Scaling
- **Description:** `11-tech-stack.md` states that Redis is used for "WebSocket fan-out" to scale horizontally. However, the standard Spring Boot STOMP configuration uses an in-memory "Simple Broker" which cannot sync subscriptions across multiple instances. 
- **Ambiguity:** Scaling STOMP horizontally typically requires a full external message broker (like RabbitMQ or ActiveMQ) via `enableStompBrokerRelay()`. Using Redis for STOMP requires a custom pub/sub bridging mechanism or a Redis-backed STOMP adapter, which is not standard in Spring.

### C-002: Database Migration Tool Selection
- **Description:** `13-migrations.md` details the SQL migration workflow and the expand/contract pattern, but does not specify whether Flyway or Liquibase is the standard tool for managing these migrations in the Spring Boot backend.

### C-003: Critical vs. Non-Critical Agent Failure Degradation
- **Description:** In `26-agent-design.md` §13, a critical agent failure (like `BackendAgent` or `FrontendAgent`) is said to abort the generation. However, `34-project-generation.md` §5 (Partial Failure Handling table) states: *"Stage 5/6 fails for an individual file... generation continues for other files, project still reaches READY (degraded)"*.
- **Contradiction:** It is unclear whether a failure to generate a single file in Backend/FrontendAgent aborts the entire pipeline or degrades gracefully.

---

## 3. Project Risks & Mitigations

| Risk ID | Category | Description | Probability / Impact | Mitigation Strategy |
|---|---|---|---|---|
| **R-001** | Security | RCE (Remote Code Execution) on backend host via malicious/faulty AI-generated test or application code. | High / Critical | Run all builds, test executions, and compiler runs inside isolated ephemeral Docker containers with CPU/Memory limits and no network access. |
| **R-002** | Financial | Runaway API cost due to infinite Self-Healing loops or malicious prompts. | Medium / High | Implement strict token/cost budgeting per user session and limit the maximum number of Self-Healing attempts per generation task (cap at 3-5). |
| **R-003** | Technical | Stale local workspace files causing sync issues between Monaco Editor (client) and local file system (server). | High / Medium | Implement WebSocket-based file change notifications and file-level optimistic locking using file hash versions. |
| **R-004** | Technical | AI Router latency and rate-limit exhaustions during parallel file generation. | High / High | Implement queueing/throttling in `AIRouterService` and use OpenRouter / local Ollama instances as robust fallbacks. |

---

## 4. Proposed Architectural Improvements

1. **Explicit Sandbox Service:** Introduce a dedicated microservice (or isolated Docker runtime worker) for running compilation commands, unit tests, and terminal processes. The main backend monolith should never execute shell commands on the host machine.
2. **Unified Event Bus:** Replace the proposed in-memory Spring event handling with a Redis-backed lightweight event bus (or Spring Application Events with Redis pub/sub) early in Phase 2 to simplify horizontal scaling.
3. **Clarified Migration Tooling:** Explicitly adopt **Flyway** for database migration management as it aligns perfectly with pure SQL migration scripts described in `13-migrations.md`.
4. **Optimistic Locking for Workspace Files:** Add a `version` or `hash` header to all file reads/writes so the frontend Monaco Editor can reject edits if the file has been modified on disk by an agent.
