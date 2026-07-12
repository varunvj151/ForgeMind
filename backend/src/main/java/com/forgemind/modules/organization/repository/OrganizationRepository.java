package com.forgemind.modules.organization.repository;

import com.forgemind.modules.organization.entity.Organization;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
  Optional<Organization> findBySlug(String slug);
  boolean existsBySlug(String slug);
}
