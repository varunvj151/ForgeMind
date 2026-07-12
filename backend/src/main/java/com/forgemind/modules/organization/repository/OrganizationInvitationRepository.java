package com.forgemind.modules.organization.repository;

import com.forgemind.modules.organization.entity.InvitationStatus;
import com.forgemind.modules.organization.entity.OrganizationInvitation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, UUID> {
  Optional<OrganizationInvitation> findByTokenHash(String tokenHash);
  List<OrganizationInvitation> findAllByOrganizationId(UUID organizationId);
  List<OrganizationInvitation> findAllByOrganizationIdAndStatus(UUID organizationId, InvitationStatus status);

  @Modifying
  @Query("UPDATE OrganizationInvitation i SET i.status = 'EXPIRED' WHERE i.status = 'PENDING' AND i.expiresAt < :now")
  int expireOldInvitations(Instant now);
}
