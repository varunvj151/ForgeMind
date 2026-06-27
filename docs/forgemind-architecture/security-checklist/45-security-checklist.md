# ForgeMind — Enterprise Security Checklist

## Table of Contents
1. Overview
2. Authentication & Session Checklist
3. Authorization Checklist
4. Data Protection Checklist
5. Input Handling Checklist
6. Infrastructure Checklist
7. AI-Specific Checklist
8. Audit & Compliance Checklist
9. Review Cadence
10. Implementation Notes
11. Future Considerations

---

## 1. Overview
This is the operational checklist auditors and engineers use to verify `24-security.md`'s design is actually implemented and maintained. Each item should be independently verifiable (test, scan, or manual review), not just a restated principle.

---

## 2. Authentication & Session Checklist
- [ ] Passwords hashed with BCrypt, cost factor ≥ 12.
- [ ] Account lockout active after 5 failed attempts / 15 minutes.
- [ ] Access JWT TTL ≤ 15 minutes; refresh JWT TTL ≤ 7 days, single-use/rotated.
- [ ] Logout invalidates the server-side session (Redis) immediately.
- [ ] JWT signing secret rotated within the last quarter.

---

## 3. Authorization Checklist
- [ ] Every endpoint touching a user-owned resource verifies ownership in the Service layer, not just role-based `@PreAuthorize`.
- [ ] `ADMIN`-only endpoints are covered by an integration test asserting `403` for `USER` role.
- [ ] No endpoint trusts a client-supplied `userId`/`projectId` without server-side ownership verification.

---

## 4. Data Protection Checklist
- [ ] TLS 1.2+ enforced at the reverse proxy/load balancer; HTTP redirects to HTTPS.
- [ ] Database and object storage encryption-at-rest confirmed enabled (provider console/config check).
- [ ] No secrets present in source control history (verified via secret-scanning tool in CI).
- [ ] `.env.example` contains only variable names, never real values.

---

## 5. Input Handling Checklist
- [ ] All DTOs have Bean Validation annotations matching `24-security.md` §5 rules.
- [ ] Workspace file paths are canonicalized and containment-checked against path traversal (covered by an explicit integration test using `../` payloads).
- [ ] AI prompt inputs are length-capped and control-character-stripped before reaching agents.
- [ ] SQL access goes exclusively through JPA/parameterized queries — no string-concatenated SQL anywhere in the codebase (verified via static analysis rule).

---

## 6. Infrastructure Checklist
- [ ] Docker images scanned for vulnerabilities before every production push (`40-cicd.md`).
- [ ] Sandbox containers (`39-docker.md`) confirmed network-isolated (no outbound internet) via a periodic automated check.
- [ ] Production secrets accessible only to the `production` GitHub Environment, not PR-triggered workflows.
- [ ] Backup restore drill performed and passed within the last quarter (`13-migrations.md` §7).

---

## 7. AI-Specific Checklist
- [ ] AI provider API keys stored server-side only, never exposed to the client.
- [ ] Egress allow-list restricts backend outbound calls to known AI provider domains plus GitHub API (`10-deployment-architecture.md`).
- [ ] Prompt templates use the `<user_input>`-style delimiting convention (`27-agent-prompts.md`) for every agent that accepts user-supplied text.
- [ ] Generated code is never executed with elevated/host-level privileges — confirmed sandbox isolation (§6) covers all code-execution paths (Self-Healing compile, Terminal commands).

---

## 8. Audit & Compliance Checklist
- [ ] `audit_logs` table actively receiving entries for all event types listed in `24-security.md` §8.
- [ ] Audit log retention configured for ≥ 1 year.
- [ ] Role-change actions (admin granting/revoking roles) are 100% covered in audit logs (spot-checked against recent admin actions).
- [ ] GitHub OAuth scope requested matches exactly what's documented in `38-github-integration.md` (no scope creep).

---

## 9. Review Cadence

| Item Group | Cadence |
|---|---|
| Authentication & Session, Authorization | Every release |
| Data Protection, Infrastructure | Quarterly |
| Input Handling | Every release (automated where possible) |
| AI-Specific | Every release + after any prompt template change |
| Audit & Compliance | Quarterly |

---

## 10. Implementation Notes
- This checklist is tracked as a living document/issue template, re-run and signed off before each major release, not a one-time audit.
- Failing any "Every release" item blocks production deployment (`40-cicd.md`'s manual approval gate is where this is enforced procedurally).

## 11. Future Considerations
- Formal third-party penetration testing once the platform reaches a user-base size that justifies the investment.
- SOC 2 readiness assessment if/when enterprise customers require it (`48-roadmap.md`).
