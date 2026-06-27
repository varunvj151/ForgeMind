# ForgeMind — Page Specifications

## Table of Contents
1. Overview
2. LandingPage
3. LoginPage
4. RegisterPage
5. DashboardPage
6. ProjectWizard (`/projects/new`)
7. ProjectExplorer (`/projects/:id`)
8. WorkspacePage (`/projects/:id/workspace`)
9. ArchitecturePage (`/projects/:id/architecture`)
10. DatabasePage (`/projects/:id/database`)
11. ApiPage (`/projects/:id/api`)
12. SettingsPage
13. AnalyticsPage
14. Implementation Notes
15. Future Considerations

---

## 1. Overview
Every route from `05-frontend.md`'s table is documented here with Purpose, UI Components, State, API Calls, Navigation, Permissions, Loading States, and Error States. This is the implementation contract for the `pages/` directory (`06-folder-structure.md`).

---

## 2. LandingPage (`/`)

| Aspect | Detail |
|---|---|
| Purpose | Marketing entry point; convert visitors to signup |
| UI Components | Hero, FeatureGrid, PricingTeaser, CTAFooter (all in `components/marketing/`) |
| State | None (static content) |
| API Calls | None |
| Navigation | → `/register` (primary CTA), → `/login` (secondary) |
| Permissions | Public |
| Loading States | N/A (static) |
| Error States | N/A |

---

## 3. LoginPage (`/login`)

| Aspect | Detail |
|---|---|
| Purpose | Authenticate existing users |
| UI Components | `AuthForm` (email/password), `SocialAuthButtons` (future), `FormError` |
| State | Local form state (react-hook-form); on success, sets auth state via Zustand `useAuthStore` |
| API Calls | `POST /api/v1/auth/login` |
| Navigation | → `/dashboard` on success; → `/register` link |
| Permissions | Public (redirects to `/dashboard` if already authenticated) |
| Loading States | Submit button shows spinner, disabled during request |
| Error States | Inline field errors (validation); banner error for `401 UNAUTHORIZED` ("Invalid email or password") |

---

## 4. RegisterPage (`/register`)

| Aspect | Detail |
|---|---|
| Purpose | Create a new account |
| UI Components | `AuthForm` (name/email/password/confirm), `PasswordStrengthMeter` |
| State | Local form state |
| API Calls | `POST /api/v1/auth/register` |
| Navigation | → `/dashboard` on success (auto-login) |
| Permissions | Public |
| Loading States | Submit spinner |
| Error States | Field validation errors; `409 CONFLICT` → "Email already registered" with link to `/login` |

---

## 5. DashboardPage (`/dashboard`)

| Aspect | Detail |
|---|---|
| Purpose | Overview of user's projects, stats, and recent activity |
| UI Components | `StatsRow`, `ProjectCardGrid`, `ActivityTimeline`, `NewProjectButton` |
| State | React Query: `projects` list, `dashboardStats`; no significant client state |
| API Calls | `GET /api/v1/projects`, `GET /api/v1/analytics/summary` (see `28`–`30` agent/router docs for AI usage stats source) |
| Navigation | → `/projects/new`, → `/projects/:id` (card click), → `/analytics` |
| Permissions | Authenticated user; shows only own projects |
| Loading States | Skeleton cards while `projects` query is pending |
| Error States | Retry banner if `projects` query fails; empty state ("No projects yet") if list is empty |

---

## 6. ProjectWizard (`/projects/new`)

| Aspect | Detail |
|---|---|
| Purpose | Multi-step project creation, ending in AI generation kickoff |
| UI Components | `WizardStepper`, `Step1NameDescription`, `Step2TechStack`, `Step3Features`, `Step4AIClarification`, `Step5Review` |
| State | Zustand `useWizardStore` (multi-step form draft, survives step navigation, cleared on submit/cancel) |
| API Calls | `POST /api/v1/projects` (creates DRAFT), `POST /api/v1/ai/generate` (final step), AI clarification round-trips via `POST /api/v1/ai/chat` |
| Navigation | → `/projects/:id/workspace` once generation starts (redirect with `jobId` query param) |
| Permissions | Authenticated user |
| Loading States | Step 4 shows `agent.thinking`-driven typing indicator while RequirementAgent asks questions (`26-agent-design.md`) |
| Error States | Per-step validation; generation-kickoff failure shows retry CTA, project remains in `DRAFT` |

---

## 7. ProjectExplorer (`/projects/:id`)

| Aspect | Detail |
|---|---|
| Purpose | Project-level overview: status, file count, quick links to sub-views |
| UI Components | `ProjectHeader`, `StatusBadge`, `QuickLinksGrid` (Workspace/Architecture/Database/API), `VersionHistoryList` |
| State | React Query: `project` detail, `project.versions` |
| API Calls | `GET /api/v1/projects/{id}`, `GET /api/v1/projects/{id}/versions` |
| Navigation | → `/projects/:id/workspace`, `/architecture`, `/database`, `/api` |
| Permissions | Owner only (403 if not owner) |
| Loading States | Skeleton header while project query pending |
| Error States | `404` → "Project not found" page; `403` → "Not authorized" page |

---

## 8. WorkspacePage (`/projects/:id/workspace`)

| Aspect | Detail |
|---|---|
| Purpose | Core IDE: browse/edit files, run builds, chat with AI (`31-workspace.md`) |
| UI Components | `FileExplorer`, `MonacoEditor`, `TerminalPanel`, `AIChatPanel`, `BuildStatusBadge` |
| State | React Query: `workspace.files` tree, `file.content` (per open file); Zustand: active file, open tabs, panel layout; WebSocket: live `build.log`, `agent.output`, `file.created` |
| API Calls | `GET /api/v1/workspace/{id}/files`, `GET /api/v1/workspace/{id}/file`, `PUT /api/v1/workspace/{id}/file`, `POST /api/v1/workspace/{id}/build`; WS subscribe to `/topic/project/{id}/*` |
| Navigation | Tab-based, in-page; → `/projects/:id` via breadcrumb |
| Permissions | Owner only |
| Loading States | File tree skeleton; editor shows spinner while fetching file content; streaming AI output renders progressively |
| Error States | File save conflict (`409`) shows merge prompt; build failure surfaces in Terminal panel with `error` WS event |

---

## 9. ArchitecturePage (`/projects/:id/architecture`)

| Aspect | Detail |
|---|---|
| Purpose | Visualize system architecture/component diagrams for the generated project |
| UI Components | `MermaidDiagramViewer`, `ComponentList` |
| State | React Query: `project.memory` (ARCHITECTURE type, `08-memory.md`) |
| API Calls | `GET /api/v1/projects/{id}/memory?type=ARCHITECTURE` |
| Navigation | → `/projects/:id/workspace` (jump to related file) |
| Permissions | Owner only |
| Loading States | Diagram skeleton |
| Error States | "No architecture generated yet" empty state for `DRAFT` projects |

---

## 10. DatabasePage (`/projects/:id/database`)

| Aspect | Detail |
|---|---|
| Purpose | ERD viewer for the generated project's schema |
| UI Components | `ERDiagramViewer`, `TableDetailPanel` |
| State | React Query: `project.memory` (DATABASE type) |
| API Calls | `GET /api/v1/projects/{id}/memory?type=DATABASE` |
| Navigation | Click table → highlights related migration file in Workspace |
| Permissions | Owner only |
| Loading States | Diagram skeleton |
| Error States | Empty state if no DB schema generated |

---

## 11. ApiPage (`/projects/:id/api`)

| Aspect | Detail |
|---|---|
| Purpose | Browsable API documentation for the generated project |
| UI Components | `EndpointList`, `EndpointDetail` (method, path, params, sample response) |
| State | React Query: `project.memory` (relevant API docs subset) |
| API Calls | `GET /api/v1/projects/{id}/memory?type=MODULE&key=api` |
| Navigation | None beyond in-page filtering |
| Permissions | Owner only |
| Loading States | List skeleton |
| Error States | Empty state if backend not yet generated |

---

## 12. SettingsPage (`/settings`)

| Aspect | Detail |
|---|---|
| Purpose | Manage account, AI provider preferences, integrations |
| UI Components | `ProfileForm`, `PasswordChangeForm`, `AIProviderPreferences`, `GitHubConnectionCard` (`38-github-integration.md`) |
| State | React Query: `user.profile`; local form state per section |
| API Calls | `GET /api/v1/users/me`, `PUT /api/v1/users/me`, `POST /api/v1/integrations/github/connect` |
| Navigation | Tabbed sub-sections within the page |
| Permissions | Authenticated user (own data only) |
| Loading States | Section-level skeletons |
| Error States | Per-form inline errors; GitHub OAuth failure shows reconnect CTA |

---

## 13. AnalyticsPage (`/analytics`)

| Aspect | Detail |
|---|---|
| Purpose | Usage stats — API counts, components generated, complexity, dev-time estimate saved |
| UI Components | `UsageChart` (Recharts), `ComplexityBreakdown`, `TimeSavedCard` |
| State | React Query: `analytics.summary` |
| API Calls | `GET /api/v1/analytics/summary` |
| Navigation | None |
| Permissions | Authenticated user (own analytics only) |
| Loading States | Chart skeletons |
| Error States | Retry banner on fetch failure |

---

## 14. Implementation Notes
- Every page component is a thin container: it wires React Query hooks and passes data to presentational components from `18-components.md`.
- Loading/error/empty states follow a shared `<QueryBoundary>` wrapper pattern to avoid repeating boilerplate across 12+ pages.
- Permissions are enforced both client-side (route guards redirecting unauthorized users) and server-side (the backend is the actual authority — see `24-security.md`).

## 15. Future Considerations
- A `TemplatesPage` (`/templates`) for the marketplace feature once it leaves MVP scope (`48-roadmap.md`).
- A `TeamSettingsPage` once multi-user/team accounts are introduced.
