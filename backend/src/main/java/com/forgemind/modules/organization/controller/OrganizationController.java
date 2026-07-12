package com.forgemind.modules.organization.controller;

import com.forgemind.modules.organization.dto.OrganizationDto.CreateOrganizationRequest;
import com.forgemind.modules.organization.dto.OrganizationDto.OrganizationResponse;
import com.forgemind.modules.organization.dto.OrganizationDto.UpdateOrganizationRequest;
import com.forgemind.modules.organization.service.OrganizationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for organization lifecycle management. */
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

  private final OrganizationService organizationService;

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<OrganizationResponse> createOrganization(
      @Valid @RequestBody CreateOrganizationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(organizationService.createOrganization(request));
  }

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<OrganizationResponse>> getMyOrganizations() {
    return ResponseEntity.ok(organizationService.getMyOrganizations());
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated() and @orgSecurity.hasRole(#id, 'VIEWER')")
  public ResponseEntity<OrganizationResponse> getOrganization(@PathVariable UUID id) {
    return ResponseEntity.ok(organizationService.getOrganizationById(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("@orgSecurity.hasRole(#id, 'ADMIN')")
  public ResponseEntity<OrganizationResponse> updateOrganization(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateOrganizationRequest request) {
    return ResponseEntity.ok(organizationService.updateOrganization(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@orgSecurity.hasRole(#id, 'OWNER')")
  public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
    organizationService.deleteOrganization(id);
    return ResponseEntity.noContent().build();
  }
}
