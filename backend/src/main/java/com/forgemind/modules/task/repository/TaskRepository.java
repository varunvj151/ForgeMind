package com.forgemind.modules.task.repository;

import com.forgemind.modules.task.entity.Task;
import com.forgemind.modules.task.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

}
