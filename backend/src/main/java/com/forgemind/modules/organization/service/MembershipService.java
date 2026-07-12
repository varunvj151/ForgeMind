package com.forgemind.modules.organization.service;

import com.forgemind.modules.organization.dto.MemberDto.InvitationResponse;
import com.forgemind.modules.organization.dto.MemberDto.InviteMemberRequest;
import com.forgemind.modules.organization.dto.MemberDto.MemberResponse;
import com.forgemind.modules.organization.dto.MemberDto.UpdateMemberRoleRequest;
import java.util.List;
import java.util.UUID;

/** Manages organization membership and email invitations. */
public interface MembershipService {

  /** Returns all members of the given organization. */
  List<MemberResponse> listMembers(UUID organizationId);

  /** Sends an invitation email to the given address. */
  InvitationResponse inviteMember(UUID organizationId, InviteMemberRequest request);

  /** Returns all pending invitations for the organization. */
  List<InvitationResponse> listInvitations(UUID organizationId);

  /** Accepts an invitation by its raw token. Creates membership if valid. */
  MemberResponse acceptInvitation(String rawToken);

  /** Cancels a pending invitation. */
  void cancelInvitation(UUID organizationId, UUID invitationId);

  /** Changes the role of an existing member. */
  MemberResponse updateMemberRole(UUID organizationId, Long userId, UpdateMemberRoleRequest request);

  /** Removes a member from the organization. */
  void removeMember(UUID organizationId, Long userId);
}
