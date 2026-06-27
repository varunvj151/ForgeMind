# ForgeMind — Component Dependency Graph

This document details the dependencies and interaction paths across all layers of the ForgeMind system, as established in the `09-48` documentation set.

---

## 1. High-Level System Dependency Graph

```mermaid
graph TD
    Client[React Frontend] -->|REST / WebSocket| Gateway[Spring Security / Gateway]
    
    subgraph Spring Boot Backend Monolith
        Gateway --> Auth[Auth Module]
        Gateway --> Proj[Project Module]
        Gateway --> WS[Workspace Module]
        Gateway --> AI[AI Generation Module]
        
        AI --> Orchestrator[Agent Orchestrator]
        Orchestrator --> Agents[Agent Pool - 10 Agents]
        Orchestrator --> Memory[Memory Module]
        
        Proj --> Memory
        Proj --> Review[Review Module]
        Proj --> Deploy[Deployment Module]
        Proj --> Templates[Templates Module]
        
        WS --> LocalFS[Local File System Manager]
    end

    subgraph Data & Caching Layers
        Auth --> Redis[(Redis Session/Cache)]
        AI --> Redis
        Proj --> Postgres[(PostgreSQL DB)]
        Memory --> Postgres
    end

    subgraph External Integrations
        Agents --> AIRouter[AI Router]
        AIRouter --> AIProviders[Gemini / Groq / OpenRouter / Ollama]
        WS --> GitHub[GitHub API]
    end
```

---

## 2. Backend Module Dependency Hierarchy

To maintain the modular monolith architecture and allow future service extraction, backend modules must strictly adhere to the following package-dependency rules (no circular dependencies allowed):

```
[API Gateway / Config]
   │
   ├──> [Auth Module] ───────────────> [Redis (Cache/Session)]
   │
   ├──> [Project Module] ────────────> [PostgreSQL (Entities/DTOs)]
   │       │
   │       ├──> [Memory Module]
   │       ├──> [Review Module]
   │       └──> [Templates Module]
   │
   ├──> [Workspace Module] ──────────> [Local File System]
   │       │
   │       └──> [Project Module] (Read-only project metadata)
   │
   └──> [AI Generation Module] ──────> [Memory Module]
           │
           └──> [Agent Orchestrator]
                   │
                   └──> [Agent Pool] ──> [AI Router] ──> [External AI Providers]
```

### Module Coupling Rules:
- Cross-module communication must happen via Java interfaces, never concrete implementations.
- No direct database joins across separate module domains (e.g., `workspace` table should not join with `review_reports` directly; request data via service interfaces).

---

## 3. Project Generation Pipeline Data Dependencies

Each generation stage relies directly on the output of the preceding stage. The pipeline cannot proceed if a critical dependency fails.

```mermaid
graph LR
    UserPrompt[User Prompt] --> Req[RequirementAgent]
    Req -->|StructuredRequirements| Plan[PlanningAgent]
    Plan -->|ProjectPlan| Arch[ArchitectureAgent]
    Arch -->|ArchitectureDesign| DBA[DatabaseAgent]
    
    DBA -->|DatabaseSchema & SQL| Backend[BackendAgent]
    Arch -->|ArchitectureDesign| Backend
    
    Backend -->|Backend Code & API Surface| Frontend[FrontendAgent]
    Arch -->|ArchitectureDesign| Frontend
    
    Backend & Frontend -->|Generated Files| Healing[Self-Healing Compile Loop]
    
    Healing -->|Verified Code| Testing[TestingAgent]
    Healing -->|Verified Code| Doc[DocumentationAgent]
    Healing -->|Verified Code| DeployAgent[DeploymentAgent]
    
    Testing & Doc & DeployAgent & Healing -->|Complete Package| Review[ReviewAgent]
    Review -->|ReviewReport| Final[READY Project Archive]
```
