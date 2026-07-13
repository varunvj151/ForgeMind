# ForgeMind Cloud Architecture

## Overview
ForgeMind has transitioned to a cloud-native, distributed architecture in Phase 11, designed to run securely and scalably on Kubernetes (EKS).

## Components

### Compute & Orchestration
- **Kubernetes (AWS EKS)**: Manages containerized workloads (Frontend, Backend).
- **Helm**: Packages all K8s manifests into deployable charts with templated variables.
- **Horizontal Pod Autoscaling (HPA)**: Automatically scales backend replicas based on CPU and memory utilization.

### Data & State
- **PostgreSQL**: Primary relational database, managed via Terraform (AWS RDS/Aurora). Contains tenant and user data, as well as `pgvector` embeddings.
- **Redis**: Distributed cache and ephemeral data store, managed via Terraform (AWS ElastiCache). Used for session management and rate limiting.
- **Object Storage (S3)**: Stores file uploads, attachments, and backups.

### Messaging
- **RabbitMQ / Kafka**: Distributed event bus supporting pub/sub messaging across multiple backend instances for webhook delivery and internal event processing.

### Observability
- **OpenTelemetry**: Integrated for distributed tracing. Requests are tagged with `X-Correlation-Id`.
- **Prometheus**: Scrapes `/actuator/prometheus` metrics from backend pods.

## Security & Isolation
- **Tenant Isolation**: Achieved via logical partitioning in PostgreSQL (tenant ID columns) and strict RBAC.
- **Network Policies**: K8s NetworkPolicies enforce zero-trust networking between pods.
- **Security Headers**: Standard OWASP headers are injected via Spring Filters (HSTS, CSP, etc.).

## Deployment
- **GitOps (ArgoCD)**: Pulls Helm charts from the repository and applies them to the cluster automatically upon merge.
