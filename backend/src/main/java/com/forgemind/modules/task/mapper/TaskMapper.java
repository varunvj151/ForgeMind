package com.forgemind.modules.task.mapper;

import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.task.dto.request.CreateTaskRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskRequest;
import com.forgemind.modules.task.dto.response.TaskResponse;
import com.forgemind.modules.task.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

  @Mapping(target = "projectId", source = "project.id")
  @Mapping(target = "projectName", source = "project.name")
  @Mapping(target = "assignee", source = "assignee")
  @Mapping(target = "createdBy", source = "createdBy")
  TaskResponse toResponse(Task task);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "project", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "assignee", ignore = true)
  @Mapping(
      target = "status",
      expression = "java(com.forgemind.modules.task.entity.TaskStatus.TODO)")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Task toEntity(CreateTaskRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "project", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "assignee", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "priority", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromRequest(UpdateTaskRequest request, @MappingTarget Task task);

  default TaskResponse.AssigneeInfo toAssigneeInfo(User user) {
    if (user == null) {
      return null;
    }
    return new TaskResponse.AssigneeInfo(
        user.getId(), user.getUsername(), user.getFirstName(), user.getLastName());
  }
}
