package com.forgemind.modules.organization.audit;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntry, UUID> {
  Page<AuditLogEntry> findAllByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);
  Page<AuditLogEntry> findAllByOrganizationIdAndActionOrderByCreatedAtDesc(
      UUID organizationId, String action, Pageable pageable);
}
