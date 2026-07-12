package com.forgemind.modules.organization.billing;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {
  Optional<UsageRecord> findByOrganizationIdAndMetricAndRecordedDate(
      UUID organizationId, UsageMetric metric, LocalDate date);
}
