# ForgeMind — Master Task List

This master task list represents the comprehensive checklist of all engineering tasks required to build the ForgeMind platform, categorized by implementation phase.

---

## Phase 1: Foundation & Setup

- [ ] **Task 1.1: Frontend Setup**
  - Initialize React + Vite + TypeScript project under `frontend/`
  - Configure TailwindCSS and ShadCN UI components
  - Set up React Router for navigation
  - Initialize Zustand for local UI state and React Query for server state
- [ ] **Task 1.2: Backend Setup**
  - Initialize Spring Boot project under `backend/` using Java 21 and Maven
  - Configure PostgreSQL database connectivity and HikariCP connection pool
  - Configure Redis connection factory for caching and sessions
  - Set up Flyway database migrations and write initial schema SQL
- [ ] **Task 1.3: Docker Containerization**
  - Create `Dockerfile` for frontend (multi-stage build with Nginx)
  - Create `Dockerfile` for backend (templated JDK 21 build)
  - Create root `docker-compose.yml` for local development setup (PostgreSQL, Redis, frontend, backend)
- [ ] **Task 1.4: Environment & Verification**
  - Create `.env.example` templates for frontend and backend
  - Verify that the local services start successfully and communicate over basic REST health check endpoints

---

## Phase 2: Core Platform Services

- [ ] **Task 2.1: Authentication & User Management**
  - Implement JWT token generation, parsing, and validation filters
  - Implement user registration, login, and token refresh endpoints
  - Implement Spring Security configuration (RBAC with `USER` and `ADMIN` roles)
- [ ] **Task 2.2: Project Management Service**
  - Implement CRUD endpoints for `Projects` in the backend
  - Implement project status state transitions (`DRAFT` -> `GENERATING` -> `READY` -> `DEPLOYED`)
  - Create Frontend Dashboard pages (`DashboardPage`, `ProjectExplorerPage`) with ShadCN components
- [ ] **Task 2.3: Workspace File Management**
  - Implement file explorer backend services to read/write files in local disk workspaces
  - Implement endpoints for listing workspace files, viewing file contents, and modifying files
  - Create Monaco Editor component in the frontend workspace
- [ ] **Task 2.4: Version Control & Snapshots**
  - Create version snapshotting service to backup/restore project workspace states
  - Implement endpoint `/api/v1/projects/{id}/versions` to list and restore snapshots

---

## Phase 3: AI Infrastructure

- [ ] **Task 3.1: AI Router Service**
  - Implement `AIRouter` interface in backend to route requests to Gemini, Groq, OpenRouter, and Ollama
  - Implement API key management and fallback-on-failure logic (with rate-limiting handles)
- [ ] **Task 3.2: Memory & Context Service**
  - Implement `MemoryService` using PostgreSQL JSONB to store architecture, database, files, and decisions
  - Implement context extraction to compile project history for agent prompts
- [ ] **Task 3.3: WebSocket Event Streaming**
  - Configure Spring STOMP WebSocket endpoints in the backend (using Redis for messaging handles)
  - Implement frontend client connection hooks to receive token-by-token generation logs and status streams

---

## Phase 4: AI Agent Pool Implementation

- [ ] **Task 4.1: Critical Pipeline Agents (1-4)**
  - Implement `RequirementAgent` (converts user prompt to structured requirements)
  - Implement `PlanningAgent` (generates project milestones and tasks)
  - Implement `ArchitectureAgent` (designs structure and validates Mermaid diagrams)
  - Implement `DatabaseAgent` (designs schema and generates SQL migrations)
- [ ] **Task 4.2: Code Generation Agents (5-6)**
  - Implement `BackendAgent` (generates controllers, services, repositories)
  - Implement `FrontendAgent` (generates React components and pages)
- [ ] **Task 4.3: Support Agents (8-10)**
  - Implement `TestingAgent` (generates JUnit and Jest test suites)
  - Implement `DocumentationAgent` (generates README.md and API guides)
  - Implement `DeploymentAgent` (generates project Dockerfiles and CI workflows)
- [ ] **Task 4.4: Review Agent (11)**
  - Implement `ReviewAgent` to audit security, naming, and architectural correctness
  - Build review reporting dashboard in the frontend

---

## Phase 5: Pipeline Orchestration & Self-Healing

- [ ] **Task 5.1: Agent Orchestrator Pipeline**
  - Implement the end-to-end execution flow coordinating all ten agents sequentially or concurrently
  - Implement transaction handling and partial failure recovery states
- [ ] **Task 5.2: Self-Healing Compile Loop**
  - Implement backend compiler execution service to compile generated Java/JS code
  - Implement error log parsing to feed compile failures back to agents for correction loops

---

## Phase 6: Interactive Features & Surgical Editing

- [ ] **Task 6.1: Workspace Chat & Surgical Editing**
  - Implement AI Chat Panel allowing chat interactions specific to the active workspace context
  - Implement surgical editing agent logic to modify specific lines of code without regenerating full files
- [ ] **Task 6.2: Live Preview Server**
  - Implement sandboxed node execution to serve generated frontend apps for live browser preview

---

## Phase 7: Deployment & Integration

- [ ] **Task 7.1: GitHub Integration**
  - Implement OAuth connection to GitHub and push-to-repo backend services
- [ ] **Task 7.2: CI/CD & Deployment**
  - Create GitHub Actions workflows for main platform verification
  - Define production Kubernetes/Cloud deployment pipelines
