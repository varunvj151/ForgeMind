package com.forgemind.modules.organization.repository;

import com.forgemind.modules.organization.entity.OrganizationMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {
  List<OrganizationMember> findAllByOrganizationId(UUID organizationId);
  List<OrganizationMember> findAllByUserId(Long userId);
  Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, Long userId);
  boolean existsByOrganizationIdAndUserId(UUID organizationId, Long userId);
  void deleteByOrganizationIdAndUserId(UUID organizationId, Long userId);
}
