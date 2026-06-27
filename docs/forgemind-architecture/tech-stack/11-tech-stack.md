# ForgeMind — Technology Stack Rationale

## Table of Contents
1. Overview
2. Frontend Stack
3. Backend Stack
4. Database & Cache
5. AI Providers
6. Communication
7. DevOps
8. Summary Decision Matrix
9. Future Scalability
10. Implementation Notes

---

## 1. Overview
This document explains **why** each technology in `01-requirements.md`'s tech stack was chosen, what alternatives were considered, and how each choice scales as ForgeMind grows toward hundreds of backend classes and React components.

---

## 2. Frontend Stack

### React + Vite + TypeScript
- **Why:** Vite's native ESM dev server gives sub-second HMR even as the component count grows into the hundreds; TypeScript gives compile-time safety across a large, AI-generated-and-human-edited codebase.
- **Alternatives considered:** Next.js (rejected — SSR/routing overhead unnecessary for a workspace-style SPA behind auth), Create React App (rejected — unmaintained, slow builds).
- **Advantages:** Fast builds, huge ecosystem, first-class TypeScript support, easy code-splitting per route (`16-ui-architecture.md`).
- **Future scalability:** Vite's Rollup-based production build supports chunking strategies that keep bundle size flat as `pages/` and `components/` grow.

### TailwindCSS + ShadCN UI
- **Why:** Utility-first CSS keeps styling co-located with markup, avoiding CSS-file sprawl across hundreds of components; ShadCN provides accessible, unstyled-by-default primitives that compose with Tailwind tokens (`19-design-system.md`).
- **Alternatives considered:** MUI (rejected — heavier, harder to theme precisely), styled-components (rejected — runtime CSS-in-JS cost at scale).
- **Advantages:** Consistent design tokens, no naming-collision risk, tree-shakeable.
- **Future scalability:** Tailwind's JIT compiler scales linearly with class usage, not component count.

### React Query
- **Why:** Server-state (projects, files, generations) needs caching, deduplication, and background refetch — exactly React Query's purpose (`20-state-management.md`).
- **Alternatives considered:** Redux + thunks (rejected — far more boilerplate for server-state), SWR (close second, React Query chosen for richer mutation/optimistic-update API).
- **Future scalability:** Query key namespacing (`project:{id}:files`) keeps cache invalidation precise as entity types multiply.

### Zustand
- **Why:** Lightweight client-state store for ephemeral UI state (active file, chat draft, editor layout) that doesn't belong in the server cache.
- **Alternatives considered:** Redux Toolkit (rejected — unnecessary ceremony for local UI state), Context API alone (rejected — re-render cost at scale).

### React Router + Framer Motion
- **Why:** Standard, well-understood routing; Framer Motion provides workspace/page transitions without custom animation code.

---

## 3. Backend Stack

### Java 21 + Spring Boot + Maven
- **Why:** Spring Boot's mature module system (`21-backend-architecture.md`) scales cleanly to hundreds of classes via package-by-feature organization; Java 21 brings virtual threads, ideal for the I/O-heavy AI orchestration workload (many concurrent provider calls).
- **Alternatives considered:** Node.js/NestJS (rejected — weaker typing discipline at large scale, less mature concurrency model for this workload), Go (rejected — ecosystem less suited to rapid CRUD + ORM development).
- **Advantages:** Strong typing, dependency injection, vast library ecosystem (Spring Security, Spring Data JPA), excellent tooling for large codebases.
- **Future scalability:** Module boundaries (`23-package-structure.md`) allow extraction into separate Spring Boot services without a rewrite.

### Spring Security + JWT
- **Why:** Industry-standard auth framework; JWT enables stateless auth that scales horizontally without sticky sessions (paired with Redis for refresh-token/session tracking, `24-security.md`).
- **Alternatives considered:** Session-cookie auth (rejected — complicates horizontal scaling), OAuth-only (deferred — may be added later for SSO, see `48-roadmap.md`).

---

## 4. Database & Cache

### PostgreSQL
- **Why:** JSONB support is essential for `project_memory` and flexible `tech_stack`/`findings` columns (`03-database.md`), while still offering relational integrity for `users`/`projects`. ACID guarantees matter for generation/version history.
- **Alternatives considered:** MongoDB (rejected — weaker relational integrity for `users`/`projects`/`generations` joins), MySQL (rejected — JSONB/GIN indexing less mature).
- **Future scalability:** Read replicas for analytics queries; partitioning `generations` by `created_at` once volume grows (`12-er-diagrams.md`).

### Redis
- **Why:** Sub-millisecond session lookups, AI response caching, rate-limiting counters, and WebSocket fan-out all need a fast, ephemeral store.
- **Alternatives considered:** In-memory Caffeine cache alone (rejected — doesn't survive instance restarts or scale across multiple backend instances).

---

## 5. AI Providers

| Provider | Role | Why |
|---|---|---|
| Gemini | Primary for architecture/requirements reasoning | Strong long-context reasoning, competitive pricing |
| Groq | Low-latency code generation | Fastest inference for high-throughput agents (Backend/Frontend agents) |
| OpenRouter | Fallback/aggregation | Access to many models through one API, useful for fallback diversity |
| Ollama | Local/offline development | Zero-cost local inference for dev/test without burning provider quota |

See `28-ai-router.md` for selection and fallback logic. Multi-provider design avoids vendor lock-in and lets cost/latency optimization happen per-agent.

---

## 6. Communication

### REST + WebSocket
- **Why:** REST for request/response CRUD (`04-api-design.md`); WebSocket for streaming AI output, build logs, and live status (`14-websocket-api.md`) — a pure-REST/polling approach would be too slow and chatty for token-by-token streaming.
- **Alternatives considered:** GraphQL (rejected — added complexity not justified at current scale; revisit in `48-roadmap.md`), Server-Sent Events (considered for one-directional streaming, WebSocket chosen for bidirectional chat).

---

## 7. DevOps

### Docker + GitHub Actions
- **Why:** Docker guarantees environment parity from a developer's laptop to production (`39-docker.md`); GitHub Actions is tightly integrated with the GitHub-hosted repo and the platform's own GitHub integration feature (`38-github-integration.md`).
- **Alternatives considered:** Jenkins (rejected — operational overhead of self-hosting), GitLab CI (rejected — repo lives on GitHub).

---

## 8. Summary Decision Matrix

| Concern | Choice | Key Driver |
|---|---|---|
| UI Framework | React + Vite + TS | DX + type safety at scale |
| Styling | Tailwind + ShadCN | Consistency without CSS sprawl |
| Server State | React Query | Caching + background sync |
| Client State | Zustand | Lightweight, no boilerplate |
| Backend Framework | Spring Boot (Java 21) | Mature DI, modularity, concurrency |
| Auth | Spring Security + JWT | Stateless, horizontally scalable |
| Primary DB | PostgreSQL | Relational + JSONB flexibility |
| Cache | Redis | Speed, multi-instance coordination |
| AI Providers | Gemini/Groq/OpenRouter/Ollama | Redundancy, cost/latency tuning |
| Realtime | WebSocket (STOMP) | Bidirectional streaming |
| CI/CD | GitHub Actions + Docker | Native GitHub integration, portability |

---

## 9. Future Scalability
- Each layer was chosen to support **horizontal scaling first**: stateless backend instances, externalized session/cache state, CDN-served static frontend.
- Module boundaries are drawn so that the AI orchestration layer, the workspace/file layer, or the review pipeline could each become an independently deployed service without changing their public interfaces (`21-backend-architecture.md`, `23-package-structure.md`).
- The provider abstraction (`AIProvider` interface, `07-ai-orchestration.md`) means new AI vendors can be added without touching agent logic.

## 10. Implementation Notes
- Pin major versions in `pom.xml`/`package.json`; upgrade on a quarterly cadence with full regression test runs (`42-testing-strategy.md`).
- Any new technology proposed after this baseline must be documented here with the same rationale format before adoption.
