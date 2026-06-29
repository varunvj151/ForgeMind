package com.forgemind.modules.project.repository;

import com.forgemind.modules.project.entity.Project;
import com.forgemind.modules.project.entity.ProjectStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Project} entities.
 *
 * <p>Spring Data JPA generates the implementation automatically. Additional query methods are added
 * here as the module grows.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

  /**
   * Returns a paginated list of projects filtered by status. Useful for dashboard views and
   * filtered lists.
   */
  Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
}
