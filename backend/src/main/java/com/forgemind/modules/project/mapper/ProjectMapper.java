package com.forgemind.modules.project.mapper;

import com.forgemind.modules.project.dto.request.ProjectRequest;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for the Project module.
 *
 * <p>MapStruct generates the implementation at compile time as a Spring {@code @Component}. All
 * three mapping directions are covered:
 *
 * <ul>
 *   <li>Entity → Response DTO
 *   <li>Request DTO → new Entity
 *   <li>Request DTO → existing Entity (partial update via {@code @MappingTarget})
 * </ul>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {

  /** Converts a Project entity to a ProjectResponse DTO. */
  @Mapping(target = "ownerId", source = "owner.id")
  @Mapping(target = "ownerUsername", source = "owner.username")
  ProjectResponse toResponse(Project project);

  /**
   * Converts a ProjectRequest DTO into a new Project entity. Server-managed fields are
   * intentionally ignored.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Project toEntity(ProjectRequest request);

  /**
   * Applies changes from a ProjectRequest onto an existing managed Project entity. Server-managed
   * fields are intentionally ignored so they are never overwritten.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromRequest(ProjectRequest request, @MappingTarget Project project);
}
