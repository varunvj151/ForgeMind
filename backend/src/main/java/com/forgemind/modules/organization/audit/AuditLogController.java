package com.forgemind.modules.organization.audit;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

  private final AuditLogService auditLogService;

  @GetMapping
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<Page<AuditLogEntry>> getAuditLog(
      @PathVariable UUID orgId, Pageable pageable) {
    return ResponseEntity.ok(auditLogService.getAuditLog(orgId, pageable));
  }
}
