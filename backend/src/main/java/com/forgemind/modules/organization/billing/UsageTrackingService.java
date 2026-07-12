package com.forgemind.modules.organization.billing;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Tracks usage metrics for organizations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsageTrackingService {

  private final UsageRecordRepository usageRecordRepository;

  /**
   * Increments a usage metric for the current day.
   * Fire-and-forget; runs in a new transaction.
   */
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void incrementUsage(UUID organizationId, UsageMetric metric, long amount) {
    try {
      LocalDate today = LocalDate.now();
      UsageRecord record = usageRecordRepository
          .findByOrganizationIdAndMetricAndRecordedDate(organizationId, metric, today)
          .orElseGet(() -> UsageRecord.builder()
              .organizationId(organizationId)
              .metric(metric)
              .recordedDate(today)
              .value(0)
              .build());

      record.setValue(record.getValue() + amount);
      usageRecordRepository.save(record);
    } catch (Exception e) {
      log.error("Failed to increment usage metric {} for org {}", metric, organizationId, e);
    }
  }
}
