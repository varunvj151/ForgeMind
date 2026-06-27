# ForgeMind — Integration Testing Strategy

## Table of Contents
1. Overview
2. Scope
3. Backend Integration Tests
4. Frontend Integration Tests
5. ArchUnit Dependency Tests
6. Test Data Management
7. CI Execution
8. Implementation Notes
9. Future Considerations

---

## 1. Overview
Integration tests verify that real module boundaries — database, cache, HTTP layer, cross-module service calls — work correctly together, complementing the mocked-collaborator unit tests in `43-unit-tests.md`.

---

## 2. Scope

| In Scope | Out of Scope (covered elsewhere) |
|---|---|
| Controller → Service → Repository → real DB round trip | Pure business logic branching (unit tests) |
| WebSocket message flow through a real Spring context | Real third-party AI provider calls (AI Smoke suite, `42-testing-strategy.md`) |
| ArchUnit dependency-rule enforcement | Full browser user journeys (E2E/Playwright) |
| Flyway migrations applying cleanly against a real PostgreSQL | Visual/UI rendering correctness (component tests) |

---

## 3. Backend Integration Tests
- **Tooling:** `@SpringBootTest` + Testcontainers (real PostgreSQL and Redis containers spun up per test class, not mocked/embedded fakes — catching real driver/SQL-dialect issues).
- Each module has an integration test suite exercising its Controller through to the database, using real Flyway migrations (`13-migrations.md`) applied to the ephemeral container.
- AI provider calls within these tests use the mocked `AIProvider` test double (`42-testing-strategy.md` §5) — integration tests verify *orchestration correctness*, not live model behavior.

Example scope (illustrative, not full implementation):
```java
@SpringBootTest
@Testcontainers
class ProjectControllerIntegrationTest {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    @Container static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Test
    void should_persistProject_and_returnIt_whenCreated() {
        // POST /api/v1/projects via MockMvc/WebTestClient
        // assert 201, assert row exists via repository
    }
}
```

---

## 4. Frontend Integration Tests
- **Tooling:** Vitest + React Testing Library + MSW (Mock Service Worker) intercepting network calls at the HTTP layer (not mocking the API client module directly), so the real `fetch`/Axios code path is exercised.
- Scope: full page flows with realistic API responses (e.g., `ProjectWizard` completing all steps and triggering `POST /ai/generate`), verifying React Query cache updates and navigation, per `20-state-management.md`.
- WebSocket-driven flows use a fake WebSocket server (e.g., `mock-socket`) to simulate `agent.output`/`file.created` events and assert UI updates accordingly.

---

## 5. ArchUnit Dependency Tests
Enforces the rules from `21-backend-architecture.md` §5 as executable tests, run in the same suite:

```java
@ArchTest
static final ArchRule controllers_should_not_depend_on_repositories =
    noClasses().that().resideInAPackage("..controller..")
        .should().dependOnClassesThat().resideInAPackage("..repository..");

@ArchTest
static final ArchRule common_should_not_depend_on_modules =
    noClasses().that().resideInAPackage("..common..")
        .should().dependOnClassesThat().resideInAPackage("..modules..");
```

These run on every CI build (`40-cicd.md`) and fail fast on architectural drift, regardless of whether functional tests still pass.

---

## 6. Test Data Management
- Each integration test class manages its own data lifecycle: seed via repository calls in `@BeforeEach`, rely on Testcontainers' fresh-container-per-class (or per-suite, with truncation between tests) for isolation — no shared mutable fixture data across test classes.
- No production or staging data is ever used in integration tests; all data is synthetically generated via the same builder helpers referenced in `43-unit-tests.md`.

---

## 7. CI Execution
- Integration tests run as a distinct Maven profile (`mvn verify -Pintegration-tests`) and npm script (`npm run test:integration`), separate from the fast unit-test run, so developers can run units locally without waiting on container startup (`40-cicd.md`'s CI workflow runs both, but developers choose locally).
- Testcontainers requires Docker-in-Docker (or an equivalent) in the CI runner — configured once in the GitHub Actions runner image, not per-workflow.

---

## 8. Implementation Notes
- Integration tests for WebSocket flows use Spring's `WebSocketStompClient` test utilities against a real (in-process) STOMP broker relay, verifying the actual message routing logic from `14-websocket-api.md`, not just unit-level publisher calls.
- Test execution time is monitored; any integration test exceeding ~10s is reviewed for unnecessary setup overhead before being accepted into the suite.

## 9. Future Considerations
- Contract testing (e.g., Pact) between frontend and backend if/when the API is consumed by additional first-party clients beyond the current SPA, reducing reliance on manual integration-test parity between the two codebases.
