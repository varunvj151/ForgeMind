package com.forgemind.modules.git.mapper;

import com.forgemind.modules.git.dto.response.GitRepositoryResponse;
import com.forgemind.modules.git.entity.GitRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GitRepositoryMapper {

  @Mapping(source = "project.id", target = "projectId")
  GitRepositoryResponse toResponse(GitRepository entity);
}
