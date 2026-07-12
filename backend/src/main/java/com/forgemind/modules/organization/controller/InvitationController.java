package com.forgemind.modules.organization.controller;

import com.forgemind.modules.organization.dto.MemberDto.MemberResponse;
import com.forgemind.modules.organization.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Public-facing endpoint for accepting organization invitations. */
@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {

  private final MembershipService membershipService;

  /**
   * Accepts an invitation using the raw token from the invitation email.
   * The user must be authenticated — they will be added to the organization.
   */
  @PostMapping("/accept")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<MemberResponse> acceptInvitation(@RequestParam String token) {
    return ResponseEntity.ok(membershipService.acceptInvitation(token));
  }
}
