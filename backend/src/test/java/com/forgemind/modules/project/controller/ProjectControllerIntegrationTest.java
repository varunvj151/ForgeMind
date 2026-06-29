package com.forgemind.modules.project.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.config.BaseIntegrationTest;
import com.forgemind.modules.auth.dto.LoginRequest;
import com.forgemind.modules.auth.dto.RegisterRequest;
import com.forgemind.modules.auth.entity.Role;
import com.forgemind.modules.auth.repository.RefreshTokenRepository;
import com.forgemind.modules.auth.repository.RoleRepository;
import com.forgemind.modules.auth.repository.UserRepository;
import com.forgemind.modules.project.dto.request.ProjectRequest;
import com.forgemind.modules.project.entity.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration tests for the Project API requiring a real PostgreSQL container.
 *
 * <p>Tagged {@code integration} — excluded from default Maven Surefire runs. Run explicitly with:
 * {@code mvn test -Dgroups=integration} or in CI via the {@code backend.yml} GitHub Actions
 * workflow.
 */
@Tag("integration")
@DisplayName("Project API — Integration Tests")
class ProjectControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private RefreshTokenRepository refreshTokenRepository;

  private String accessToken;

  private static final String REGISTER_URL = "/api/v1/auth/register";
  private static final String LOGIN_URL = "/api/v1/auth/login";

  @BeforeEach
  void setUp() throws Exception {
    // Clean state before each test
    refreshTokenRepository.deleteAll();
    userRepository.deleteAll();
    roleRepository.deleteAll();

    roleRepository.save(Role.builder().name("ROLE_USER").description("Standard user").build());

    // Register + login to obtain a real JWT
    RegisterRequest reg =
        new RegisterRequest(
            "Integration", "User", "integration_user", "integration@example.com", "password123");

    mockMvc
        .perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
        .andExpect(status().isCreated());

    MvcResult loginResult =
        mockMvc
            .perform(
                post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new LoginRequest("integration_user", "password123"))))
            .andExpect(status().isOk())
            .andReturn();

    accessToken =
        objectMapper
            .readTree(loginResult.getResponse().getContentAsString())
            .get("accessToken")
            .asText();
  }

  @Test
  @DisplayName("POST /api/projects — returns 201 with correct data")
  void createProject_Success() throws Exception {
    ProjectRequest request =
        new ProjectRequest("Integration Project", "Desc", ProjectStatus.ACTIVE);

    mockMvc
        .perform(
            post("/api/projects")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Integration Project"))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.ownerUsername").value("integration_user"));
  }

  @Test
  @DisplayName("POST /api/projects — unauthenticated returns 401")
  void createProject_Unauthenticated() throws Exception {
    ProjectRequest request =
        new ProjectRequest("Unauthorized Project", "Desc", ProjectStatus.ACTIVE);

    mockMvc
        .perform(
            post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }
}
