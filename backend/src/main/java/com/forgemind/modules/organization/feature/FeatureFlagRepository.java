package com.forgemind.modules.organization.feature;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {
  Optional<FeatureFlag> findByOrganizationIdAndFlagName(UUID organizationId, PlanFeature flagName);
}
