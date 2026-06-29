package com.forgemind.modules.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.auth.dto.RegisterRequest;
import com.forgemind.modules.auth.entity.Role;
import com.forgemind.modules.auth.repository.RoleRepository;
import com.forgemind.modules.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link AuthController} registration endpoint.
 *
 * <p>Uses an in-memory H2 database via the {@code test} profile. Redis is mocked so no external
 * services are required. Each test runs in a transaction that is rolled back on completion.
 *
 * <p>Note: Flyway is disabled in the test profile, so we seed the {@code ROLE_USER} role manually
 * in {@code @BeforeEach}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("POST /api/v1/auth/register — Integration Tests")
class AuthControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @MockBean private RedisConnectionFactory redisConnectionFactory;

  @MockBean private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    roleRepository.deleteAll();
    // Flyway is disabled in tests — seed the default role manually
    roleRepository.save(
        Role.builder().name("ROLE_USER").description("Standard authenticated user").build());
  }

  // ── Success ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("201 — valid request creates user and returns public profile")
  void register_success_returns201() throws Exception {
    RegisterRequest request =
        new RegisterRequest("Alice", "Smith", "alicesmith", "alice@example.com", "password123");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.username").value("alicesmith"))
        .andExpect(jsonPath("$.email").value("alice@example.com"))
        .andExpect(jsonPath("$.firstName").value("Alice"))
        .andExpect(jsonPath("$.lastName").value("Smith"))
        .andExpect(jsonPath("$.role[0]").value("ROLE_USER"))
        .andExpect(jsonPath("$.createdAt").isNotEmpty())
        // CRITICAL: password MUST never appear in response
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
  }

  @Test
  @DisplayName("201 — email is stored in lowercase")
  void register_emailStoredLowercase() throws Exception {
    RegisterRequest request =
        new RegisterRequest("Bob", "Jones", "bobjones", "BOB@EXAMPLE.COM", "password123");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("bob@example.com"));
  }

  // ── Conflict (409) ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("409 — duplicate username returns conflict")
  void register_duplicateUsername_returns409() throws Exception {
    RegisterRequest first =
        new RegisterRequest("Carol", "White", "carolwhite", "carol@example.com", "password123");
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)))
        .andExpect(status().isCreated());

    RegisterRequest duplicate =
        new RegisterRequest("Carol", "White", "carolwhite", "carol2@example.com", "password123");
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("USERNAME_EXISTS"));
  }

  @Test
  @DisplayName("409 — duplicate email returns conflict")
  void register_duplicateEmail_returns409() throws Exception {
    RegisterRequest first =
        new RegisterRequest("Dave", "Green", "davegreen", "dave@example.com", "password123");
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)))
        .andExpect(status().isCreated());

    RegisterRequest duplicate =
        new RegisterRequest("Dave", "Green", "davegreen2", "dave@example.com", "password123");
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("EMAIL_EXISTS"));
  }

  // ── Validation (400) ───────────────────────────────────────────────────────

  @Test
  @DisplayName("400 — invalid email format")
  void register_invalidEmail_returns400() throws Exception {
    RegisterRequest request =
        new RegisterRequest("Eve", "Brown", "evebrown", "not-an-email", "password123");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
  }

  @Test
  @DisplayName("400 — password shorter than 8 characters")
  void register_shortPassword_returns400() throws Exception {
    RegisterRequest request =
        new RegisterRequest("Frank", "Hill", "frankhill", "frank@example.com", "short");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
  }

  @Test
  @DisplayName("400 — blank firstName fails validation")
  void register_blankFirstName_returns400() throws Exception {
    RegisterRequest request =
        new RegisterRequest("", "Hill", "frankhill2", "frank2@example.com", "password123");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
  }

  @Test
  @DisplayName("400 — blank username fails validation")
  void register_blankUsername_returns400() throws Exception {
    RegisterRequest request =
        new RegisterRequest("Grace", "Lee", "", "grace@example.com", "password123");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
  }
}
