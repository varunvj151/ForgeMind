package com.forgemind.modules.organization.service;

import com.forgemind.modules.organization.dto.OrganizationDto.CreateOrganizationRequest;
import com.forgemind.modules.organization.dto.OrganizationDto.OrganizationResponse;
import com.forgemind.modules.organization.dto.OrganizationDto.UpdateOrganizationRequest;
import java.util.List;
import java.util.UUID;

/** Contract for organization (tenant) lifecycle management. */
public interface OrganizationService {

  /** Creates a new organization owned by the currently authenticated user. */
  OrganizationResponse createOrganization(CreateOrganizationRequest request);

  /** Returns the organization with the given ID. Throws if not found. */
  OrganizationResponse getOrganizationById(UUID id);

  /** Returns the organization with the given slug. Throws if not found. */
  OrganizationResponse getOrganizationBySlug(String slug);

  /** Returns all organizations the current user belongs to. */
  List<OrganizationResponse> getMyOrganizations();

  /** Updates mutable fields on the organization. Requires ADMIN or OWNER. */
  OrganizationResponse updateOrganization(UUID id, UpdateOrganizationRequest request);

  /** Soft-deletes the organization (sets status = DELETED). Requires OWNER. */
  void deleteOrganization(UUID id);
}
