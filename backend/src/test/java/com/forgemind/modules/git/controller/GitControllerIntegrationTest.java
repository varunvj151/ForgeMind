package com.forgemind.modules.git.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.git.dto.request.ConnectRepositoryRequest;
import com.forgemind.modules.git.dto.response.GitRepositoryResponse;
import com.forgemind.modules.git.provider.GitProviderType;
import com.forgemind.modules.git.service.GitRepositoryService;
import com.forgemind.modules.git.service.GitSyncService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GitController.class)
class GitControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private GitRepositoryService gitRepositoryService;
  @MockBean private GitSyncService gitSyncService;
  @MockBean private com.forgemind.modules.auth.security.JwtService jwtService;
  @MockBean private com.forgemind.modules.auth.security.CurrentUserProvider currentUserProvider;
  @MockBean private com.forgemind.modules.organization.ratelimit.InMemoryRateLimiter rateLimiter;

  private User mockUser;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setId(1L);
    mockUser.setUsername("testuser");

    // Bypass security for web layer tests (assuming a standard filter config, but we mock the context)
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(mockUser, null, List.of())
    );

    // Bypass rate limits so tests never get 429
    when(rateLimiter.tryConsume(anyString())).thenReturn(true);
    when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt())).thenReturn(true);
    when(rateLimiter.getRemaining(anyString())).thenReturn(999L);
  }

  @Test
  void testConnectRepository() throws Exception {
    ConnectRepositoryRequest request = new ConnectRepositoryRequest();
    request.setProjectId(UUID.randomUUID());
    request.setProvider(GitProviderType.GITHUB);
    request.setOwner("testOwner");
    request.setRepoName("testRepo");
    request.setAccessToken("token123");

    GitRepositoryResponse response = GitRepositoryResponse.builder()
        .id(UUID.randomUUID())
        .provider("GITHUB")
        .owner("testOwner")
        .repoName("testRepo")
        .build();

    when(gitRepositoryService.connectRepository(any(), any())).thenReturn(response);

    mockMvc.perform(post("/api/v1/git/repositories")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.provider").value("GITHUB"))
        .andExpect(jsonPath("$.owner").value("testOwner"))
        .andExpect(jsonPath("$.repoName").value("testRepo"));
  }

  @Test
  void testListRepositories() throws Exception {
    UUID projectId = UUID.randomUUID();
    GitRepositoryResponse response = GitRepositoryResponse.builder()
        .id(UUID.randomUUID())
        .provider("GITHUB")
        .owner("testOwner")
        .repoName("testRepo")
        .build();

    when(gitRepositoryService.listProjectRepositories(any(), any())).thenReturn(List.of(response));

    mockMvc.perform(get("/api/v1/git/repositories")
            .param("projectId", projectId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].provider").value("GITHUB"));
  }
}
