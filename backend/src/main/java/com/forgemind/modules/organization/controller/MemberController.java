package com.forgemind.modules.organization.controller;

import com.forgemind.modules.organization.dto.MemberDto.InvitationResponse;
import com.forgemind.modules.organization.dto.MemberDto.InviteMemberRequest;
import com.forgemind.modules.organization.dto.MemberDto.MemberResponse;
import com.forgemind.modules.organization.dto.MemberDto.UpdateMemberRoleRequest;
import com.forgemind.modules.organization.service.MembershipService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/members")
@RequiredArgsConstructor
public class MemberController {

  private final MembershipService membershipService;

  @GetMapping
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'VIEWER')")
  public ResponseEntity<List<MemberResponse>> listMembers(@PathVariable UUID orgId) {
    return ResponseEntity.ok(membershipService.listMembers(orgId));
  }

  @PostMapping("/invite")
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<InvitationResponse> inviteMember(
      @PathVariable UUID orgId,
      @Valid @RequestBody InviteMemberRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(membershipService.inviteMember(orgId, request));
  }

  @GetMapping("/invitations")
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<List<InvitationResponse>> listInvitations(@PathVariable UUID orgId) {
    return ResponseEntity.ok(membershipService.listInvitations(orgId));
  }

  @DeleteMapping("/invitations/{invitationId}")
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<Void> cancelInvitation(
      @PathVariable UUID orgId, @PathVariable UUID invitationId) {
    membershipService.cancelInvitation(orgId, invitationId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{userId}/role")
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<MemberResponse> updateRole(
      @PathVariable UUID orgId,
      @PathVariable Long userId,
      @Valid @RequestBody UpdateMemberRoleRequest request) {
    return ResponseEntity.ok(membershipService.updateMemberRole(orgId, userId, request));
  }

  @DeleteMapping("/{userId}")
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<Void> removeMember(
      @PathVariable UUID orgId, @PathVariable Long userId) {
    membershipService.removeMember(orgId, userId);
    return ResponseEntity.noContent().build();
  }
}
