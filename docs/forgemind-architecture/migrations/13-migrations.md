# ForgeMind — Database Migration Strategy

## Table of Contents
1. Overview
2. Tooling
3. Versioning Convention
4. Migration Workflow
5. Rollback Strategy
6. Seed Data
7. Backup Strategy
8. Implementation Notes
9. Future Considerations

---

## 1. Overview
ForgeMind uses **versioned, forward-only SQL migrations** to evolve the schema defined in `03-database.md` and `12-er-diagrams.md`. Migrations are the only sanctioned way to change schema — no manual DDL against any environment beyond local dev.

---

## 2. Tooling
- **Flyway** (Spring Boot's default integration) manages migrations from `backend/src/main/resources/db/migration/`.
- Migrations run automatically on backend startup for `local` and `staging` profiles; production runs them as an explicit pre-deploy CI step (`40-cicd.md`) so schema changes are reviewed before the app boots against them.

---

## 3. Versioning Convention

| Pattern | Example | Use |
|---|---|---|
| `V{n}__{description}.sql` | `V1__init_schema.sql` | Versioned, applied once, in order |
| `R__{description}.sql` | `R__refresh_views.sql` | Repeatable, re-applied on checksum change |

Rules:
- `n` is a strictly increasing integer; never reuse or renumber a version once merged to `main`.
- One logical change per migration (one new table, one column addition, one index) to keep rollbacks granular.
- Migration filenames are descriptive snake_case, e.g. `V7__add_project_versions_table.sql`.

---

## 4. Migration Workflow

```mermaid
graph LR
    DEV[Developer writes V{n}__change.sql] --> LOCAL[Run against local DB]
    LOCAL --> TEST[Integration tests run migration]
    TEST --> PR[Open PR]
    PR --> CI[CI applies migration to ephemeral DB]
    CI --> MERGE[Merge to main]
    MERGE --> STAGING[Auto-apply on staging deploy]
    STAGING --> PROD[Manual-gated apply on production deploy]
```

Every migration PR must include:
1. The forward SQL.
2. An entry in this document's changelog table (below) is optional for minor changes, mandatory for any change touching `users`, `projects`, or `project_memory`.
3. Confirmation that `12-er-diagrams.md` constraints still hold.

---

## 5. Rollback Strategy
Flyway does not auto-generate rollback scripts; ForgeMind's policy:

| Migration Type | Rollback Approach |
|---|---|
| Additive (new table/column, nullable) | Roll forward with a corrective migration; do not delete in place |
| Destructive (drop column/table) | Require a two-step migration: deprecate (stop writing) in release N, drop in release N+2, after confirming no read paths remain |
| Data migration | Always paired with a `_down.sql` companion script kept in `db/rollback/` for manual execution during an incident |

Production schema changes that are destructive require a feature flag or dual-write period — never a same-release drop.

---

## 6. Seed Data
- `db/migration/seed/` contains idempotent seed scripts (e.g., default `ADMIN` user for local/staging, starter `templates` rows) gated behind the `local` and `staging` Spring profiles only — **never run against production automatically**.
- Seed scripts use `INSERT ... ON CONFLICT DO NOTHING` to remain safely re-runnable.

---

## 7. Backup Strategy

| Environment | Frequency | Retention | Mechanism |
|---|---|---|---|
| Local | None required | — | — |
| Staging | Daily snapshot | 7 days | Managed PostgreSQL automated snapshot |
| Production | Continuous WAL archiving + daily full snapshot | 35 days | Managed PostgreSQL PITR (point-in-time recovery) |

Restore drills are run quarterly against a scratch instance to validate backup integrity (tracked in `45-security-checklist.md`).

---

## 8. Implementation Notes
- All migrations run inside a transaction where the DDL supports it; PostgreSQL allows most DDL to be transactional, simplifying failure recovery.
- Long-running migrations (large backfills) are split into batched application-level jobs (`25-background-jobs.md`) rather than a single blocking `ALTER TABLE`.
- CI fails the build if a previously-applied migration file's checksum changes — migrations are immutable once merged.

## 9. Future Considerations
- Adopt blue/green schema migration patterns for zero-downtime destructive changes once uptime SLAs tighten.
- Evaluate `pgroll` or expand/contract tooling as schema change frequency increases with AI-driven feature growth.
