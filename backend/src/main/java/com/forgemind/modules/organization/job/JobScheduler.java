package com.forgemind.modules.organization.job;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Submits background jobs for execution. */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobScheduler {

  private final BackgroundJobRepository jobRepository;
  private final JobExecutor jobExecutor;

  /**
   * Schedules a job and immediately triggers the async executor.
   */
  @Transactional
  public void scheduleJob(UUID organizationId, String jobType, String payload) {
    BackgroundJob job = BackgroundJob.builder()
        .organizationId(organizationId)
        .jobType(jobType)
        .payload(payload)
        .build();
    job = jobRepository.save(job);
    log.info("Scheduled background job: id={}, type={}, org={}", job.getId(), jobType, organizationId);
    
    // Hand off to the async executor
    jobExecutor.executeAsync(job.getId());
  }
}
