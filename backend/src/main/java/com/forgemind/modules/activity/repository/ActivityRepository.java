package com.forgemind.modules.activity.repository;

import com.forgemind.modules.activity.entity.Activity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

  Page<Activity> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);

  Page<Activity> findByTeamIdOrderByCreatedAtDesc(UUID teamId, Pageable pageable);

  Page<Activity> findByActorIdOrderByCreatedAtDesc(Long actorId, Pageable pageable);
}
