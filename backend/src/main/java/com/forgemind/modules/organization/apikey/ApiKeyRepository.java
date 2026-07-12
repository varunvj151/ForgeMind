package com.forgemind.modules.organization.apikey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
  Optional<ApiKey> findByTokenHash(String tokenHash);
  List<ApiKey> findAllByOrganizationId(UUID organizationId);
  List<ApiKey> findAllByOrganizationIdAndUserId(UUID organizationId, Long userId);

  @Modifying
  @Query("UPDATE ApiKey k SET k.revoked = true WHERE k.organizationId = :orgId")
  int revokeAllByOrganizationId(UUID orgId);
}
