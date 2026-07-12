package com.forgemind.modules.git.service;

import com.forgemind.modules.git.dto.request.ConnectRepositoryRequest;
import com.forgemind.modules.git.dto.response.GitRepositoryResponse;
import com.forgemind.modules.git.entity.GitRepository;
import com.forgemind.modules.git.mapper.GitRepositoryMapper;
import com.forgemind.modules.git.provider.GitProvider;
import com.forgemind.modules.git.provider.GitProviderFactory;
import com.forgemind.modules.git.repository.GitRepositoryRepository;
import com.forgemind.modules.project.entity.Project;
import com.forgemind.modules.project.repository.ProjectRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GitRepositoryServiceImpl implements GitRepositoryService {

  private final GitRepositoryRepository gitRepositoryRepository;
  private final ProjectRepository projectRepository;
  private final GitProviderFactory gitProviderFactory;
  private final GitRepositoryMapper gitRepositoryMapper;
  private final CodeIndexingService codeIndexingService;

  public GitRepositoryServiceImpl(
      GitRepositoryRepository gitRepositoryRepository,
      ProjectRepository projectRepository,
      GitProviderFactory gitProviderFactory,
      GitRepositoryMapper gitRepositoryMapper,
      CodeIndexingService codeIndexingService) {
    this.gitRepositoryRepository = gitRepositoryRepository;
    this.projectRepository = projectRepository;
    this.gitProviderFactory = gitProviderFactory;
    this.gitRepositoryMapper = gitRepositoryMapper;
    this.codeIndexingService = codeIndexingService;
  }

  @Override
  @Transactional
  public GitRepositoryResponse connectRepository(ConnectRepositoryRequest request, Long userId) {
    Project project = projectRepository.findById(request.getProjectId())
        .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        
    // Validate provider token and fetch metadata
    GitProvider provider = gitProviderFactory.getProvider(request.getProvider());
    GitRepository repo = provider.getRepository(request.getOwner(), request.getRepoName(), request.getAccessToken());
    
    repo.setProject(project);
    GitRepository saved = gitRepositoryRepository.save(repo);
    
    return gitRepositoryMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public void disconnectRepository(UUID repositoryId, Long userId) {
    GitRepository repo = getRepositoryEntity(repositoryId);
    // Delete vectors
    codeIndexingService.deleteRepositoryIndex(repositoryId);
    // JPA cascade will handle branches, commits, PRs, and CodeChunks (minus the vectors if they were in a separate collection, but here they are just rows in code_chunks which cascade deletes)
    gitRepositoryRepository.delete(repo);
  }

  @Override
  @Transactional(readOnly = true)
  public GitRepositoryResponse getRepository(UUID repositoryId, Long userId) {
    return gitRepositoryMapper.toResponse(getRepositoryEntity(repositoryId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<GitRepositoryResponse> listProjectRepositories(UUID projectId, Long userId) {
    return gitRepositoryRepository.findAllByProjectId(projectId).stream()
        .map(gitRepositoryMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  public GitRepository getRepositoryEntity(UUID repositoryId) {
    return gitRepositoryRepository.findById(repositoryId)
        .orElseThrow(() -> new IllegalArgumentException("Repository not found"));
  }
}
