# ForgeMind — Unit Testing Standards

## Table of Contents
1. Overview
2. Backend Unit Test Standards
3. Frontend Unit Test Standards
4. Naming Conventions
5. Structure: Given-When-Then
6. Mocking Rules
7. Example Patterns
8. Implementation Notes
9. Future Considerations

---

## 1. Overview
Concrete, enforceable standards for unit tests, implementing the "Unit" layer of `42-testing-strategy.md`'s pyramid.

---

## 2. Backend Unit Test Standards
- **Framework:** JUnit 5 + Mockito.
- One test class per production class: `ProjectService` → `ProjectServiceTest`.
- Tests target the Service layer primarily (`21-backend-architecture.md`); Controllers are covered by integration tests (`44-integration-tests.md`) since their main job — HTTP binding — is best verified with a real request/response cycle.
- Repositories are not unit-tested directly (Spring Data JPA query methods are framework-verified); custom `@Query` methods are covered by integration tests against a real (Testcontainers) database.

---

## 3. Frontend Unit Test Standards
- **Framework:** Vitest + React Testing Library.
- Test components by **behavior visible to the user** (rendered text, accessible roles), not implementation details (internal state, instance methods).
- Hooks are tested via `renderHook` with explicit assertions on returned state/functions across state transitions.
- Zustand stores are tested directly as plain functions/state objects — no need to render a component to verify store logic.

---

## 4. Naming Conventions

| Context | Convention | Example |
|---|---|---|
| Backend test class | `{ClassUnderTest}Test` | `ProjectServiceTest` |
| Backend test method | `should_{expectedBehavior}_when_{condition}` | `should_throwNotFound_when_projectDoesNotExist` |
| Frontend test file | `{ComponentName}.test.tsx` | `ProjectCard.test.tsx` |
| Frontend test case | Plain-English `it('does X when Y')` | `it('shows a status badge matching the project status')` |

---

## 5. Structure: Given-When-Then

Backend example (illustrative, not full implementation):
```java
@Test
void should_throwNotFound_when_projectDoesNotExist() {
    // Given
    UUID missingId = UUID.randomUUID();
    when(projectRepository.findById(missingId)).thenReturn(Optional.empty());

    // When / Then
    assertThrows(ProjectNotFoundException.class,
        () -> projectService.getProject(missingId, currentUserId));
}
```

Frontend example (illustrative, not full implementation):
```tsx
it('shows a status badge matching the project status', () => {
  // Given
  const project = buildProject({ status: 'GENERATING' });

  // When
  render(<ProjectCard project={project} />);

  // Then
  expect(screen.getByText(/generating/i)).toBeVisible();
});
```

---

## 6. Mocking Rules
- Mock **collaborators**, never the class under test.
- Backend: mock repositories, external clients (`AIProvider`, `GitHubApiClient`, `FileStorageAdapter`); never mock simple value objects or DTOs.
- Frontend: mock API calls via MSW handlers (shared fixtures across tests) rather than ad-hoc `vi.mock()` of fetch in every test file, for consistency.
- AI provider responses are mocked with realistic, schema-valid canned payloads (`42-testing-strategy.md` §5) sourced from the shared `test-fixtures` module.

---

## 7. Example Patterns

| Pattern | When to Use |
|---|---|
| Parameterized tests (`@ParameterizedTest`, Vitest `it.each`) | Same logic, multiple input/output pairs (e.g., status-transition validation) |
| Builder helpers (`buildProject()`, `buildUser()`) | Constructing test fixtures without repeating full object literals everywhere |
| Snapshot testing | Used sparingly for stable, structural output (e.g., generated Mermaid diagram structure) — avoided for anything involving LLM-variable text |

---

## 8. Implementation Notes
- Test fixtures/builders live alongside the module under test (`modules/projects/test/ProjectFixtures.java`) for backend, and a shared `test-utils/` folder for frontend, per `23-package-structure.md`-consistent placement.
- No test depends on execution order or shared mutable state between tests — each test sets up and tears down its own fixtures.

## 9. Future Considerations
- Adopt mutation testing (`42-testing-strategy.md` Future Considerations) to validate assertion strength once the suite matures.
