# Disaster Recovery (DR) Plan

## Overview
This document outlines the Disaster Recovery protocols for ForgeMind to ensure High Availability (HA) and minimal data loss.

## Recovery Point Objective (RPO) and Recovery Time Objective (RTO)
- **RPO**: 1 hour (Data loss will not exceed 1 hour of changes).
- **RTO**: 4 hours (The system will be restored to a fully operational state within 4 hours).

## Database Backups (PostgreSQL)
- **Automated Backups**: RDS/Aurora performs automated daily snapshots with a 7-day retention period.
- **Continuous Archiving**: Write-Ahead Logs (WAL) are shipped to S3 for point-in-time recovery (PITR) up to the last 5 minutes.
- **Cross-Region Replication**: Asynchronous read replicas in a secondary region can be promoted to master in case of a primary region outage.

## Storage Backups (S3)
- **Versioning**: Object versioning is enabled on all S3 buckets to prevent accidental overwrites or deletions.
- **Cross-Region Replication (CRR)**: S3 buckets are replicated to a secondary region.

## Infrastructure Recovery (Terraform)
Because infrastructure is managed via Terraform, a complete cluster can be spun up in a secondary region within minutes:
1. Update `terraform/environments/production/terraform.tfvars` with the secondary region variables.
2. Run `terraform apply`.
3. Update DNS routing (Route53) to point `api.forgemind.dev` and `app.forgemind.dev` to the new Ingress controller.

## Application Recovery (GitOps)
- ArgoCD automatically monitors the `forgemind-infrastructure` repository.
- Once the new Kubernetes cluster is provisioned and ArgoCD is installed, it will automatically sync the Helm charts and deploy the application.
