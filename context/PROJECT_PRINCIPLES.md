# ForgeMind Project Principles

## Purpose

This document defines the non-negotiable engineering principles for the ForgeMind platform. Every contributor, whether human or AI, must follow these principles.

---

## 1. Architecture First

No implementation may contradict the approved architecture documentation.

---

## 2. Single Source of Truth

The `docs/forgemind-architecture/` directory is the authoritative source for requirements, architecture, APIs, database design, and workflows.

---

## 3. Build in Small Increments

Implement one task at a time.

Never build multiple unrelated features in a single iteration.

---

## 4. Production Quality

Every implementation must be suitable for production.

No placeholder logic unless explicitly requested.

---

## 5. Clean Code

Follow:

* SOLID
* DRY
* KISS
* Separation of Concerns
* Clean Architecture

---

## 6. Security by Default

All code must be secure by design.

Never expose secrets.

Validate every input.

Use least-privilege access.

---

## 7. AI Provider Independence

ForgeMind must not depend on a single AI provider.

The system should support multiple providers through an abstraction layer.

---

## 8. Explainability

Every AI-generated change should include an explanation of:

* Files changed
* Reason for change
* Impact
* Risks

---

## 9. Backward Compatibility

New features should not break existing functionality unless intentionally planned.

---

## 10. Documentation

Every major implementation must update the relevant documentation.

Documentation is part of the deliverable.
