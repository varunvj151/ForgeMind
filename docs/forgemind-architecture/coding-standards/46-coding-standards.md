# ForgeMind — Coding Standards

## Table of Contents
1. Overview
2. Backend (Java/Spring) Standards
3. Frontend (React/TypeScript) Standards
4. AI Module Standards
5. API Standards
6. Documentation Standards
7. Test Standards
8. Formatting & Tooling
9. Implementation Notes
10. Future Considerations

---

## 1. Overview
Concrete conventions every contributor — human or AI agent — follows, enforced via linters/static analysis in CI (`40-cicd.md`) wherever automatable.

---

## 2. Backend (Java/Spring) Standards

| Rule | Detail |
|---|---|
| Naming | Classes: `PascalCase`. Methods/fields: `camelCase`. Constants: `UPPER_SNAKE_CASE`. Packages: lowercase, no underscores. |
| DTO suffixes | Requests end in `Request`, responses end in `Response`. |
| Layering | Strictly follow `21-backend-architecture.md`; no business logic in Controllers, no HTTP concerns in Services. |
| Null handling | Prefer `Optional<T>` for nullable return types from Services; never return `null` from a public Service method. |
| Exceptions | Always extend `ForgemindException`; never throw raw `RuntimeException`. |
| Logging | Use SLF4J via Lombok's `@Slf4j`; never log secrets, tokens, or full request bodies containing PII. |
| Immutability | Prefer `record` types for DTOs (Java 21) over mutable POJOs. |

---

## 3. Frontend (React/TypeScript) Standards

| Rule | Detail |
|---|---|
| Naming | Components: `PascalCase` files and exports. Hooks: `camelCase`, `use` prefix. Stores: `use{Name}Store`. |
| Typing | No `any` without an inline comment justifying it; prefer explicit interfaces over inferred shapes for props. |
| Components | Functional components only; no class components. Presentational components stay pure (no direct data fetching). |
| State | Follow `20-state-management.md`'s category rules strictly — server state never duplicated into Zustand. |
| Styling | Tailwind utility classes + design tokens (`19-design-system.md`) only; no inline `style={{}}` except for truly dynamic values (e.g., computed widths). |
| Imports | Absolute imports from `src/` root (no deep `../../../` chains) via configured path aliases. |

---

## 4. AI Module Standards

| Rule | Detail |
|---|---|
| Provider access | Agents never import a provider SDK directly; always go through `AIProvider`/`AIRouter` (`28-ai-router.md`). |
| Prompts | Externalized as template files (`27-agent-prompts.md`), never inlined as Java string literals. |
| Output handling | Every agent output is schema-validated before being trusted by the Orchestrator (`26-agent-design.md`). |
| Statelessness | Agents hold no per-request mutable instance state; all context flows through explicit parameters (`GenerationContext`). |

---

## 5. API Standards
- Follow `04-api-design.md`'s REST conventions and `15-api-versioning.md`'s compatibility rules for every new/changed endpoint.
- Every new endpoint requires: Bean Validation on its request DTO, an entry in the OpenAPI spec (auto-generated via springdoc), and at least one integration test (`44-integration-tests.md`).
- Error responses always use the standard envelope (`15-api-versioning.md` §5) — no endpoint returns a bespoke error shape.

---

## 6. Documentation Standards
- Every new backend module gets a short `README.md` inside its package root summarizing its responsibility (one paragraph) and linking to the relevant architecture doc (this 48-document set).
- Public Service interfaces carry Javadoc on every method describing the contract (not the implementation) — what it does, what it throws, not how.
- React components with non-obvious props get a brief JSDoc comment above the component.

---

## 7. Test Standards
- Follow `43-unit-tests.md` and `44-integration-tests.md` exactly — naming, Given-When-Then structure, mocking rules.
- No PR merges with a coverage regression below the thresholds in `42-testing-strategy.md` §6.

---

## 8. Formatting & Tooling

| Concern | Tool |
|---|---|
| Java formatting | Spotless (Google Java Format) |
| Java static analysis | Checkstyle + ArchUnit |
| TypeScript formatting | Prettier |
| TypeScript linting | ESLint (with `typescript-eslint`, `react-hooks` plugin) |
| Commit messages | Conventional Commits (`feat:`, `fix:`, `chore:`, etc.) |

All formatting is auto-applied via pre-commit hooks; CI fails on unformatted code rather than relying on developer discipline alone.

---

## 9. Implementation Notes
- These standards are deliberately the same constraints injected into BackendAgent/FrontendAgent prompts (`27-agent-prompts.md`) — AI-generated and human-written code are held to one shared standard, not two.
- Standards violations found by `ReviewAgent`'s Naming pass (`37-code-review.md`) reference this document directly in their `recommendation` text.

## 10. Future Considerations
- Auto-fixable lint rule expansion as common AI-generated code patterns reveal new conventions worth codifying (feedback loop with `27-agent-prompts.md`).
