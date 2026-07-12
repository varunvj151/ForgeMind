package com.forgemind.modules.organization.job;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Executes background jobs asynchronously. */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobExecutor {

  private final BackgroundJobRepository jobRepository;

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void executeAsync(UUID jobId) {
    BackgroundJob job = jobRepository.findById(jobId).orElse(null);
    if (job == null || job.getStatus() != JobStatus.PENDING) {
      return; // Already processed or deleted
    }

    try {
      job.setStatus(JobStatus.RUNNING);
      job.setStartedAt(Instant.now());
      jobRepository.saveAndFlush(job);

      // --- TODO: Actual execution logic based on job.getJobType() goes here ---
      log.info("Executing job {} of type {}", jobId, job.getJobType());
      // Simulate work for the stub
      Thread.sleep(100);

      job.setStatus(JobStatus.COMPLETED);
    } catch (Exception e) {
      log.error("Job {} failed", jobId, e);
      job.setStatus(JobStatus.FAILED);
      job.setErrorMessage(e.getMessage());
    } finally {
      job.setCompletedAt(Instant.now());
      jobRepository.save(job);
    }
  }
}
