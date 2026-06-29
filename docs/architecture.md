# ForgeMind Architecture

This document provides a high-level overview of the ForgeMind platform's architecture.

## System Context

ForgeMind is a Kanban Task Management system designed as a modern, real-time SaaS platform.

```mermaid
C4Context
  title System Context diagram for ForgeMind
  
  Person(user, "User", "A software engineer or project manager")
  System(forgemind, "ForgeMind", "Task Management Platform")
  
  Rel(user, forgemind, "Manages projects and tasks", "HTTPS/WSS")
```

## Container Diagram

The system is broken down into a frontend SPA and a Spring Boot backend, backed by PostgreSQL and Redis.

```mermaid
C4Container
  title Container diagram for ForgeMind
  
  Person(user, "User", "A software engineer or project manager")
  
  Container(spa, "Single Page Application", "React, TypeScript, Vite", "Provides the Kanban UI and real-time updates")
  Container(api, "API Application", "Java, Spring Boot", "Handles business logic, auth, and REST/WebSocket APIs")
  ContainerDb(db, "Database", "PostgreSQL", "Stores user, project, and task data")
  ContainerDb(cache, "Cache / PubSub", "Redis", "Handles STOMP WebSocket message brokering and caching")
  
  Rel(user, spa, "Visits", "HTTPS")
  Rel(spa, api, "Makes API calls to", "JSON/HTTPS")
  Rel(spa, api, "Receives real-time updates from", "STOMP/WSS")
  Rel(api, db, "Reads from and writes to", "JDBC")
  Rel(api, cache, "Publishes/Subscribes to events", "RESP")
```

## Data Model (Entity Relationship)

```mermaid
erDiagram
    USER ||--o{ TEAM_MEMBER : has
    TEAM ||--o{ TEAM_MEMBER : includes
    TEAM ||--o{ PROJECT : owns
    PROJECT ||--o{ TASK : contains
    USER ||--o{ TASK : assigned_to

    USER {
        uuid id PK
        string username
        string email
        string password
    }
    TEAM {
        uuid id PK
        string name
    }
    PROJECT {
        uuid id PK
        uuid team_id FK
        string name
        string description
    }
    TASK {
        uuid id PK
        uuid project_id FK
        uuid assignee_id FK
        string title
        string description
        string status
        string priority
    }
```

## CI/CD Pipeline

The project utilizes GitHub Actions for continuous integration and continuous deployment.

```mermaid
graph LR
    A[Push to Main] --> B[GitHub Actions]
    B --> C{Parallel Jobs}
    C --> D[Backend: Maven Build & Test]
    C --> E[Frontend: Vitest & Playwright]
    D --> F[Build Docker Images]
    E --> F
    F --> G[Push to Registry]
    G --> H[Deploy via Docker Compose]
```

## Monitoring & Observability

ForgeMind uses Micrometer, Prometheus, and Grafana for monitoring application health, along with MDC-enriched structured logging for tracing.

```mermaid
graph TD
    A[Spring Boot API] -->|Exposes /actuator/prometheus| B(Prometheus)
    B -->|Scrapes Metrics| A
    C[Grafana] -->|Queries| B
    A -->|Structured Logs (MDC)| D(Log File/Fluentd)
```
