package com.forgemind.modules.organization.feature;

import com.forgemind.modules.organization.entity.Organization;
import com.forgemind.modules.organization.entity.OrganizationPlan;
import com.forgemind.modules.organization.repository.OrganizationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checks if a feature is enabled for an organization.
 * Combines plan-level defaults with per-organization overrides.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureFlagService {

  private final FeatureFlagRepository featureFlagRepository;
  private final OrganizationRepository organizationRepository;

  @Transactional(readOnly = true)
  public boolean isFeatureEnabled(UUID organizationId, PlanFeature feature) {
    // 1. Check for specific override
    return featureFlagRepository.findByOrganizationIdAndFlagName(organizationId, feature)
        .map(FeatureFlag::isEnabled)
        .orElseGet(() -> {
          // 2. Fall back to plan default
          Organization org = organizationRepository.findById(organizationId).orElse(null);
          if (org == null) return false;
          return isEnabledByDefault(org.getPlan(), feature);
        });
  }

  private boolean isEnabledByDefault(OrganizationPlan plan, PlanFeature feature) {
    return switch (plan) {
      case ENTERPRISE, BUSINESS -> true;
      case PRO -> feature == PlanFeature.ADVANCED_AI;
      case FREE -> false;
    };
  }
}
