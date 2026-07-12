package com.forgemind.modules.organization.job;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BackgroundJobRepository extends JpaRepository<BackgroundJob, UUID> {
  
  @Query("SELECT j FROM BackgroundJob j WHERE j.status = 'PENDING' ORDER BY j.createdAt ASC")
  List<BackgroundJob> findPendingJobs(Pageable pageable);
}
