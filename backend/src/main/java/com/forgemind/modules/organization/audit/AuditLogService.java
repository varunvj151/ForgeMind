package com.forgemind.modules.organization.audit;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for recording audit log entries.
 *
 * <p>Audit writes are performed asynchronously so they never slow down the
 * primary request path. Each entry is written in its own transaction to
 * prevent rollback from discarding audit data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;

  /**
   * Asynchronously records an audit event. Fire-and-forget — never throws.
   *
   * @param organizationId the tenant scope (may be null for global events)
   * @param actorId        ID of the user who performed the action
   * @param action         action name, e.g. "MEMBER_INVITED", "API_KEY_REVOKED"
   * @param resourceType   entity type, e.g. "Organization", "ApiKey"
   * @param resourceId     string ID of the affected resource
   * @param metadata       optional JSON metadata string
   */
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void log(UUID organizationId, Long actorId, String action,
                  String resourceType, String resourceId, String metadata) {
    try {
      AuditLogEntry entry = AuditLogEntry.builder()
          .organizationId(organizationId)
          .actorId(actorId)
          .action(action)
          .resourceType(resourceType)
          .resourceId(resourceId)
          .metadata(metadata)
          .build();
      auditLogRepository.save(entry);
    } catch (Exception e) {
      log.error("Failed to write audit log: action={}, org={}", action, organizationId, e);
    }
  }

  /** Returns a paginated list of audit log entries for the given organization. */
  @Transactional(readOnly = true)
  public Page<AuditLogEntry> getAuditLog(UUID organizationId, Pageable pageable) {
    return auditLogRepository.findAllByOrganizationIdOrderByCreatedAtDesc(organizationId, pageable);
  }
}
