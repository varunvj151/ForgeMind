package com.forgemind.modules.git.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.forgemind.modules.git.dto.request.ConnectRepositoryRequest;
import com.forgemind.modules.git.dto.response.GitRepositoryResponse;
import com.forgemind.modules.git.entity.GitRepository;
import com.forgemind.modules.git.mapper.GitRepositoryMapper;
import com.forgemind.modules.git.provider.GitProvider;
import com.forgemind.modules.git.provider.GitProviderFactory;
import com.forgemind.modules.git.provider.GitProviderType;
import com.forgemind.modules.git.repository.GitRepositoryRepository;
import com.forgemind.modules.project.entity.Project;
import com.forgemind.modules.project.repository.ProjectRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GitRepositoryServiceImplTest {

  @Mock private GitRepositoryRepository gitRepositoryRepository;
  @Mock private ProjectRepository projectRepository;
  @Mock private GitProviderFactory gitProviderFactory;
  @Mock private GitRepositoryMapper gitRepositoryMapper;
  @Mock private CodeIndexingService codeIndexingService;
  @Mock private GitProvider mockProvider;

  private GitRepositoryServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new GitRepositoryServiceImpl(
        gitRepositoryRepository,
        projectRepository,
        gitProviderFactory,
        gitRepositoryMapper,
        codeIndexingService);
  }

  @Test
  void testConnectRepository() {
    UUID projectId = UUID.randomUUID();
    ConnectRepositoryRequest request = new ConnectRepositoryRequest();
    request.setProjectId(projectId);
    request.setProvider(GitProviderType.GITHUB);
    request.setOwner("testOwner");
    request.setRepoName("testRepo");
    request.setAccessToken("token123");

    Project project = new Project();
    project.setId(projectId);

    GitRepository entity = new GitRepository();
    entity.setId(UUID.randomUUID());
    entity.setProvider(GitProviderType.GITHUB);
    entity.setOwner("testOwner");
    entity.setRepoName("testRepo");

    GitRepositoryResponse responseDto = GitRepositoryResponse.builder()
        .id(entity.getId())
        .provider("GITHUB")
        .build();

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(gitProviderFactory.getProvider(GitProviderType.GITHUB)).thenReturn(mockProvider);
    when(mockProvider.getRepository("testOwner", "testRepo", "token123")).thenReturn(entity);
    when(gitRepositoryRepository.save(any(GitRepository.class))).thenReturn(entity);
    when(gitRepositoryMapper.toResponse(entity)).thenReturn(responseDto);

    GitRepositoryResponse result = service.connectRepository(request, 1L);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(entity.getId());
  }
}
