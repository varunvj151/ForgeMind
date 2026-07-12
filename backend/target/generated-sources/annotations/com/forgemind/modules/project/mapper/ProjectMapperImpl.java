package com.forgemind.modules.project.mapper;

import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.project.dto.request.ProjectRequest;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.entity.Project;
import com.forgemind.modules.project.entity.ProjectStatus;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-11T21:31:41+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Override
    public ProjectResponse toResponse(Project project) {
        if ( project == null ) {
            return null;
        }

        Long ownerId = null;
        String ownerUsername = null;
        UUID id = null;
        String name = null;
        String description = null;
        ProjectStatus status = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        ownerId = projectOwnerId( project );
        ownerUsername = projectOwnerUsername( project );
        id = project.getId();
        name = project.getName();
        description = project.getDescription();
        status = project.getStatus();
        createdAt = project.getCreatedAt();
        updatedAt = project.getUpdatedAt();

        ProjectResponse projectResponse = new ProjectResponse( id, name, description, status, createdAt, updatedAt, ownerId, ownerUsername );

        return projectResponse;
    }

    @Override
    public Project toEntity(ProjectRequest request) {
        if ( request == null ) {
            return null;
        }

        Project.ProjectBuilder project = Project.builder();

        project.name( request.name() );
        project.description( request.description() );
        project.status( request.status() );

        return project.build();
    }

    @Override
    public void updateEntityFromRequest(ProjectRequest request, Project project) {
        if ( request == null ) {
            return;
        }

        project.setName( request.name() );
        project.setDescription( request.description() );
        project.setStatus( request.status() );
    }

    private Long projectOwnerId(Project project) {
        if ( project == null ) {
            return null;
        }
        User owner = project.getOwner();
        if ( owner == null ) {
            return null;
        }
        Long id = owner.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String projectOwnerUsername(Project project) {
        if ( project == null ) {
            return null;
        }
        User owner = project.getOwner();
        if ( owner == null ) {
            return null;
        }
        String username = owner.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }
}
