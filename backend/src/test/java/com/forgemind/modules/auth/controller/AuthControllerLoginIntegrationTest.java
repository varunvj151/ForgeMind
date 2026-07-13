package com.forgemind.modules.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.auth.dto.LoginRequest;
import com.forgemind.modules.auth.dto.RegisterRequest;
import com.forgemind.modules.auth.entity.Role;
import com.forgemind.modules.auth.repository.RefreshTokenRepository;
import com.forgemind.modules.auth.repository.RoleRepository;
import com.forgemind.modules.auth.repository.UserRepository;
import com.forgemind.modules.organization.ratelimit.InMemoryRateLimiter;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@code POST /api/v1/auth/login} endpoint.
 *
 * <p>Uses H2 in-memory DB via the {@code test} profile. Redis is mocked. A real user is registered
 * before each test using the registration endpoint so that login can be verified end-to-end against
 * actual BCrypt hashes.
 *
 * <p>Each test is transactional and rolled back, ensuring isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("POST /api/v1/auth/login — Integration Tests")
class AuthControllerLoginIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @MockBean private RedisConnectionFactory redisConnectionFactory;
  @MockBean private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;
  @MockBean private InMemoryRateLimiter rateLimiter;

  private static final String REGISTER_URL = "/api/v1/auth/register";
  private static final String LOGIN_URL = "/api/v1/auth/login";

  @BeforeEach
  void setUp() throws Exception {
    // Wipe and re-seed — Flyway is disabled in test profile
    refreshTokenRepository.deleteAll();
    userRepository.deleteAll();
    roleRepository.deleteAll();
    roleRepository.save(
        Role.builder().name("ROLE_USER").description("Standard authenticated user").build());

    // Bypass rate limits so tests never get 429
    when(rateLimiter.tryConsume(anyString())).thenReturn(true);
    when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt())).thenReturn(true);
    when(rateLimiter.getRemaining(anyString())).thenReturn(999L);

    // Register a user that all login tests can use
    RegisterRequest reg =
        new RegisterRequest("Alice", "Smith", "alicesmith", "alice@example.com", "password123");
    mockMvc
        .perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
        .andExpect(status().isCreated());
  }

  // ── Success (200) ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("200 — login by username returns tokens and user profile")
  void login_byUsername_returns200WithTokens() throws Exception {
    LoginRequest req = new LoginRequest("alicesmith", "password123");

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.refreshToken").isNotEmpty())
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresIn").isNumber())
        .andExpect(jsonPath("$.user.username").value("alicesmith"))
        .andExpect(jsonPath("$.user.email").value("alice@example.com"))
        .andExpect(jsonPath("$.user.firstName").value("Alice"))
        .andExpect(jsonPath("$.user.lastName").value("Smith"))
        .andExpect(jsonPath("$.user.role[0]").value("ROLE_USER"))
        .andExpect(jsonPath("$.user.createdAt").isNotEmpty())
        // CRITICAL: password must NEVER appear
        .andExpect(jsonPath("$.user.password").doesNotExist())
        .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
  }

  @Test
  @DisplayName("200 — login by email returns tokens and correct user")
  void login_byEmail_returns200() throws Exception {
    LoginRequest req = new LoginRequest("alice@example.com", "password123");

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.username").value("alicesmith"))
        .andExpect(jsonPath("$.accessToken").isNotEmpty());
  }

  @Test
  @DisplayName("200 — login by email is case-insensitive")
  void login_byEmailUppercase_returns200() throws Exception {
    LoginRequest req = new LoginRequest("ALICE@EXAMPLE.COM", "password123");

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.email").value("alice@example.com"));
  }

  @Test
  @DisplayName("200 — access token is a valid 3-part JWT")
  void login_accessTokenIsJwt() throws Exception {
    LoginRequest req = new LoginRequest("alicesmith", "password123");

    MvcResult result =
        mockMvc
            .perform(
                post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn();

    String body = result.getResponse().getContentAsString();
    String accessToken = objectMapper.readTree(body).get("accessToken").asText();

    // JWT has exactly 3 base64url segments separated by dots
    assertThat(accessToken.split("\\.")).hasSize(3);
  }

  @Test
  @DisplayName("200 — refresh token is persisted in the database")
  void login_refreshTokenIsPersistedInDb() throws Exception {
    LoginRequest req = new LoginRequest("alicesmith", "password123");

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk());

    // Exactly one refresh token should be persisted after login
    assertThat(refreshTokenRepository.count()).isEqualTo(1);
    var token = refreshTokenRepository.findAll().getFirst();
    assertThat(token.isRevoked()).isFalse();
    assertThat(token.getExpiresAt()).isAfter(java.time.Instant.now());
  }

  @Test
  @DisplayName("200 — expiresIn matches configured access-token duration in seconds")
  void login_expiresInMatchesConfig() throws Exception {
    LoginRequest req = new LoginRequest("alicesmith", "password123");

    MvcResult result =
        mockMvc
            .perform(
                post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn();

    long expiresIn =
        objectMapper.readTree(result.getResponse().getContentAsString()).get("expiresIn").asLong();

    // Test profile uses expiration-ms: 3600000 → 3600 seconds
    assertThat(expiresIn).isEqualTo(3600L);
  }

  // ── Conflict / Unauthorized (401) ─────────────────────────────────────────

  @Test
  @DisplayName("401 — wrong password returns INVALID_CREDENTIALS, not 500")
  void login_wrongPassword_returns401() throws Exception {
    LoginRequest req = new LoginRequest("alicesmith", "wrongpassword");

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
        .andExpect(jsonPath("$.status").value(401));
  }

  @Test
  @DisplayName("401 — unknown username returns INVALID_CREDENTIALS")
  void login_unknownUsername_returns401() throws Exception {
    LoginRequest req = new LoginRequest("nobody", "password123");

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
  }

  @Test
  @DisplayName("401 — unknown email returns INVALID_CREDENTIALS")
  void login_unknownEmail_returns401() throws Exception {
    LoginRequest req = new LoginRequest("ghost@example.com", "password123");

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
  }

  @Test
  @DisplayName("401 — error body never reveals whether identifier or password was wrong")
  void login_errorMessageIsOpaque() throws Exception {
    // Wrong password
    MvcResult wrongPass =
        mockMvc
            .perform(
                post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new LoginRequest("alicesmith", "wrongpassword"))))
            .andReturn();

    // Unknown user
    MvcResult unknownUser =
        mockMvc
            .perform(
                post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(new LoginRequest("ghost", "password123"))))
            .andReturn();

    String msgWrongPass =
        objectMapper.readTree(wrongPass.getResponse().getContentAsString()).at("/message").asText();
    String msgUnknownUser =
        objectMapper
            .readTree(unknownUser.getResponse().getContentAsString())
            .at("/message")
            .asText();

    assertThat(msgWrongPass).isEqualTo(msgUnknownUser);
  }

  // ── Validation (400) ──────────────────────────────────────────────────────

  @Test
  @DisplayName("400 — blank usernameOrEmail fails validation")
  void login_blankIdentifier_returns400() throws Exception {
    LoginRequest req = new LoginRequest("", "password123");

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
  }

  @Test
  @DisplayName("400 — blank password fails validation")
  void login_blankPassword_returns400() throws Exception {
    LoginRequest req = new LoginRequest("alicesmith", "");

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
  }
}
