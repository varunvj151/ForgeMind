package com.forgemind.modules.git.mapper;

import com.forgemind.modules.git.dto.response.GitCommitResponse;
import com.forgemind.modules.git.entity.GitCommit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GitCommitMapper {

  @Mapping(source = "repository.id", target = "repositoryId")
  GitCommitResponse toResponse(GitCommit entity);
}
