# ForgeMind — Sprint Plan (Phase 0 MVP)

This document organizes the development of the ForgeMind Phase 0 MVP into structured, 2-week sprints. Each sprint has a clear goal, deliverables, and dependencies.

---

## Sprint 1: Local Foundation & Dev Environment Setup
- **Goal:** Set up local repositories, containerization, database schemas, and establish basic connectivity.
- **Deliverables:**
  - Vite/React/TS app template with Tailwind and ShadCN configured.
  - Spring Boot backend skeleton running Java 21 with PostgreSQL and Redis connections.
  - Flyway migrations setup with initial schema.
  - Docker Compose file running local services.
- **Dependencies:** None.

---

## Sprint 2: Core Platform Services
- **Goal:** Build the basic workspace user flow: project management, directory listing, and code viewing.
- **Deliverables:**
  - Stateless JWT registration and login endpoints (Spring Security integration).
  - Project CRUD endpoints and database persistence.
  - Directory scanning and file CRUD endpoints for workspace paths.
  - Frontend dashboard pages, project creation dialog, and workspace shell containing the Monaco Editor.
- **Dependencies:** Sprint 1.

---

## Sprint 3: AI Infrastructure & Streaming
- **Goal:** Implement the AI Router, local workspace memory, and WebSocket streaming connection.
- **Deliverables:**
  - `AIRouterService` supporting Gemini, Groq, OpenRouter API calls with retry-on-failure handling.
  - `MemoryService` using PostgreSQL GIN indexed JSONB columns to load/save prompt decisions.
  - Spring WebSocket STOMP messaging setup with client subscriber hooks.
  - Workspace Chat Panel template in the frontend.
- **Dependencies:** Sprint 2.

---

## Sprint 4: Pipeline Orchestration & Analysis Agents
- **Goal:** Orchestrate the planning phase of project generation (Stages 1 through 4).
- **Deliverables:**
  - Pipeline coordinator state engine to step through generation.
  - `RequirementAgent` & `PlanningAgent` service classes and prompt templates.
  - `ArchitectureAgent` & `DatabaseAgent` services (generates and parses schema + Mermaid charts).
  - Stepper UI component in frontend showing progress of these first four stages.
- **Dependencies:** Sprint 3.

---

## Sprint 5: Code Generation & Self-Healing
- **Goal:** Generate backend/frontend source code and run compilation-based verification loops.
- **Deliverables:**
  - `BackendAgent` and `FrontendAgent` prompt templates and service generators.
  - Compile loop executing backend javac/maven build commands in workspace.
  - Log parsers extracting compilation/type errors and feeding them back to agents.
- **Dependencies:** Sprint 4.

---

## Sprint 6: Quality Assurance & Review
- **Goal:** Add support agents (Testing, Documentation, Deployment) and compile code audit reports.
- **Deliverables:**
  - `TestingAgent` generating JUnit/Jest tests.
  - `DocumentationAgent` creating README/API files.
  - `DeploymentAgent` generating Docker/GitHub Action files.
  - `ReviewAgent` executing static analysis prompts and generating `ReviewReport`.
  - Frontend report view displaying review score and findings.
- **Dependencies:** Sprint 5.

---

## Sprint 7: Workspace Evolution & Git Integration
- **Goal:** Allow incremental edits via workspace chat, push projects to GitHub, and finalize MVP verification.
- **Deliverables:**
  - Surgical-edit prompt agent allowing chat commands to modify code blocks.
  - GitHub integration using OAuth to push generated project repository.
  - Comprehensive end-to-end testing of generation pipeline.
- **Dependencies:** Sprint 6.
