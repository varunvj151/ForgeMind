package com.forgemind.modules.git.service;

import com.forgemind.modules.git.dto.request.ConnectRepositoryRequest;
import com.forgemind.modules.git.dto.response.GitRepositoryResponse;
import com.forgemind.modules.git.entity.GitRepository;
import java.util.List;
import java.util.UUID;

public interface GitRepositoryService {
  GitRepositoryResponse connectRepository(ConnectRepositoryRequest request, Long userId);
  void disconnectRepository(UUID repositoryId, Long userId);
  GitRepositoryResponse getRepository(UUID repositoryId, Long userId);
  List<GitRepositoryResponse> listProjectRepositories(UUID projectId, Long userId);
  
  // Internal access
  GitRepository getRepositoryEntity(UUID repositoryId);
}
