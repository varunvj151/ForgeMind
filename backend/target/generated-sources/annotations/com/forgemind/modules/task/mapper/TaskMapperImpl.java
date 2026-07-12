package com.forgemind.modules.task.mapper;

import com.forgemind.modules.project.entity.Project;
import com.forgemind.modules.task.dto.request.CreateTaskRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskRequest;
import com.forgemind.modules.task.dto.response.TaskResponse;
import com.forgemind.modules.task.entity.Task;
import com.forgemind.modules.task.entity.TaskPriority;
import com.forgemind.modules.task.entity.TaskStatus;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-11T21:31:42+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class TaskMapperImpl implements TaskMapper {

    @Override
    public TaskResponse toResponse(Task task) {
        if ( task == null ) {
            return null;
        }

        UUID projectId = null;
        String projectName = null;
        TaskResponse.AssigneeInfo assignee = null;
        TaskResponse.AssigneeInfo createdBy = null;
        UUID id = null;
        String title = null;
        String description = null;
        TaskStatus status = null;
        TaskPriority priority = null;
        Instant dueDate = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        projectId = taskProjectId( task );
        projectName = taskProjectName( task );
        assignee = toAssigneeInfo( task.getAssignee() );
        createdBy = toAssigneeInfo( task.getCreatedBy() );
        id = task.getId();
        title = task.getTitle();
        description = task.getDescription();
        status = task.getStatus();
        priority = task.getPriority();
        dueDate = task.getDueDate();
        createdAt = task.getCreatedAt();
        updatedAt = task.getUpdatedAt();

        TaskResponse taskResponse = new TaskResponse( id, projectId, projectName, title, description, status, priority, assignee, createdBy, dueDate, createdAt, updatedAt );

        return taskResponse;
    }

    @Override
    public Task toEntity(CreateTaskRequest request) {
        if ( request == null ) {
            return null;
        }

        Task.TaskBuilder task = Task.builder();

        task.title( request.title() );
        task.description( request.description() );
        task.priority( request.priority() );
        task.dueDate( request.dueDate() );

        task.status( com.forgemind.modules.task.entity.TaskStatus.TODO );

        return task.build();
    }

    @Override
    public void updateEntityFromRequest(UpdateTaskRequest request, Task task) {
        if ( request == null ) {
            return;
        }

        task.setTitle( request.title() );
        task.setDescription( request.description() );
        task.setDueDate( request.dueDate() );
    }

    private UUID taskProjectId(Task task) {
        if ( task == null ) {
            return null;
        }
        Project project = task.getProject();
        if ( project == null ) {
            return null;
        }
        UUID id = project.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String taskProjectName(Task task) {
        if ( task == null ) {
            return null;
        }
        Project project = task.getProject();
        if ( project == null ) {
            return null;
        }
        String name = project.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
