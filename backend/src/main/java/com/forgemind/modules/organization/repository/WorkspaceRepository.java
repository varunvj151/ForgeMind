package com.forgemind.modules.organization.repository;

import com.forgemind.modules.organization.entity.Workspace;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
  List<Workspace> findAllByOrganizationId(UUID organizationId);
  Optional<Workspace> findByOrganizationIdAndSlug(UUID organizationId, String slug);
  boolean existsByOrganizationIdAndSlug(UUID organizationId, String slug);
}
