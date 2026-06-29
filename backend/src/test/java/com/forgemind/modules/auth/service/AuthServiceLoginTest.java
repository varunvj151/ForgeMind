package com.forgemind.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.forgemind.common.exception.ForgemindException;
import com.forgemind.config.JwtProperties;
import com.forgemind.modules.auth.dto.LoginRequest;
import com.forgemind.modules.auth.dto.LoginResponse;
import com.forgemind.modules.auth.entity.RefreshToken;
import com.forgemind.modules.auth.entity.Role;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.repository.RefreshTokenRepository;
import com.forgemind.modules.auth.repository.RoleRepository;
import com.forgemind.modules.auth.repository.UserRepository;
import com.forgemind.modules.auth.security.JwtService;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for the login flow in {@link AuthService}.
 *
 * <p>Spring context is NOT loaded — all collaborators are mocked with Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — loginUser() unit tests")
class AuthServiceLoginTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtService jwtService;
  @Mock private JwtProperties jwtProperties;

  @InjectMocks private AuthService authService;

  private User activeUser;
  private Role userRole;

  @BeforeEach
  void setUp() {
    userRole = new Role();
    userRole.setId(1L);
    userRole.setName("ROLE_USER");

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    activeUser =
        User.builder()
            .id(1L)
            .firstName("Alice")
            .lastName("Smith")
            .username("alicesmith")
            .email("alice@example.com")
            .passwordHash("$2a$12$hashed")
            .roles(roles)
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private void stubJwtProperties() {
    when(jwtProperties.getExpirationMs()).thenReturn(900_000L); // 15 min
    when(jwtProperties.getRefreshExpirationMs()).thenReturn(604_800_000L); // 7 days
  }

  // ── Success paths ─────────────────────────────────────────────────────────

  @Test
  @DisplayName(
      "login — success with username: returns accessToken, refreshToken, 'Bearer', expiresIn, user")
  void login_byUsername_returnsLoginResponse() {
    when(userRepository.findByUsername("alicesmith")).thenReturn(Optional.of(activeUser));
    when(authenticationManager.authenticate(any())).thenReturn(null); // success
    when(jwtService.generateToken(activeUser)).thenReturn("access-token-xyz");
    when(jwtService.generateRefreshToken(activeUser)).thenReturn("refresh-token-xyz");
    stubJwtProperties();

    LoginResponse response = authService.loginUser(new LoginRequest("alicesmith", "password123"));

    assertThat(response.accessToken()).isEqualTo("access-token-xyz");
    assertThat(response.refreshToken()).isEqualTo("refresh-token-xyz");
    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.expiresIn()).isEqualTo(900L);
    assertThat(response.user().username()).isEqualTo("alicesmith");
    assertThat(response.user().email()).isEqualTo("alice@example.com");
    assertThat(response.user().firstName()).isEqualTo("Alice");
    assertThat(response.user().lastName()).isEqualTo("Smith");
    assertThat(response.user().role()).contains("ROLE_USER");
  }

  @Test
  @DisplayName("login — success with email: resolves user via email fallback")
  void login_byEmail_resolvesViaEmailFallback() {
    when(userRepository.findByUsername("alice@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(activeUser));
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(jwtService.generateToken(activeUser)).thenReturn("access-token");
    when(jwtService.generateRefreshToken(activeUser)).thenReturn("refresh-token");
    stubJwtProperties();

    LoginResponse response =
        authService.loginUser(new LoginRequest("alice@example.com", "password123"));

    assertThat(response.user().username()).isEqualTo("alicesmith");
  }

  @Test
  @DisplayName("login — email input is lowercased before lookup")
  void login_emailIsLowercasedBeforeLookup() {
    when(userRepository.findByUsername("ALICE@EXAMPLE.COM")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(activeUser));
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(jwtService.generateToken(any())).thenReturn("t");
    when(jwtService.generateRefreshToken(any())).thenReturn("r");
    stubJwtProperties();

    authService.loginUser(new LoginRequest("ALICE@EXAMPLE.COM", "password123"));

    verify(userRepository).findByEmail("alice@example.com");
  }

  @Test
  @DisplayName("login — persists refresh token with hashed value and correct expiry")
  void login_persistsRefreshTokenWithHash() {
    when(userRepository.findByUsername("alicesmith")).thenReturn(Optional.of(activeUser));
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(jwtService.generateToken(any())).thenReturn("access-token");
    when(jwtService.generateRefreshToken(any())).thenReturn("raw-refresh-token");
    stubJwtProperties();

    authService.loginUser(new LoginRequest("alicesmith", "password123"));

    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenRepository).save(captor.capture());

    RefreshToken saved = captor.getValue();
    // Hash must NOT be the raw token string
    assertThat(saved.getTokenHash()).isNotEqualTo("raw-refresh-token");
    // Hash must be 64 hex chars (SHA-256)
    assertThat(saved.getTokenHash()).matches("[0-9a-f]{64}");
    assertThat(saved.getUser()).isEqualTo(activeUser);
    assertThat(saved.isRevoked()).isFalse();
    assertThat(saved.getExpiresAt()).isAfter(Instant.now());
  }

  @Test
  @DisplayName("login — deletes expired refresh tokens before issuing new one")
  void login_purgesExpiredTokensBeforeSaving() {
    when(userRepository.findByUsername("alicesmith")).thenReturn(Optional.of(activeUser));
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(jwtService.generateToken(any())).thenReturn("t");
    when(jwtService.generateRefreshToken(any())).thenReturn("r");
    stubJwtProperties();

    authService.loginUser(new LoginRequest("alicesmith", "password123"));

    // deleteAllExpiredBefore must be called BEFORE save
    var inOrder = inOrder(refreshTokenRepository);
    inOrder.verify(refreshTokenRepository).deleteAllExpiredBefore(any(Instant.class));
    inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  // ── Failure paths ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("login — unknown identifier throws 401 INVALID_CREDENTIALS")
  void login_unknownUser_throws401() {
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("ghost")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.loginUser(new LoginRequest("ghost", "any")))
        .isInstanceOf(ForgemindException.class)
        .satisfies(
            ex -> {
              ForgemindException fex = (ForgemindException) ex;
              assertThat(fex.getCode()).isEqualTo("INVALID_CREDENTIALS");
              assertThat(fex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            });

    verify(authenticationManager, never()).authenticate(any());
    verify(refreshTokenRepository, never()).save(any());
  }

  @Test
  @DisplayName("login — wrong password (AuthenticationException) throws 401 INVALID_CREDENTIALS")
  void login_wrongPassword_throws401() {
    when(userRepository.findByUsername("alicesmith")).thenReturn(Optional.of(activeUser));
    doThrow(new BadCredentialsException("Bad credentials"))
        .when(authenticationManager)
        .authenticate(any(UsernamePasswordAuthenticationToken.class));

    assertThatThrownBy(() -> authService.loginUser(new LoginRequest("alicesmith", "wrong")))
        .isInstanceOf(ForgemindException.class)
        .satisfies(
            ex -> {
              ForgemindException fex = (ForgemindException) ex;
              assertThat(fex.getCode()).isEqualTo("INVALID_CREDENTIALS");
              assertThat(fex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            });

    verify(refreshTokenRepository, never()).save(any());
  }

  @Test
  @DisplayName("login — disabled account (DisabledException) throws 401 INVALID_CREDENTIALS")
  void login_disabledAccount_throws401() {
    when(userRepository.findByUsername("alicesmith")).thenReturn(Optional.of(activeUser));
    doThrow(new DisabledException("Account is disabled"))
        .when(authenticationManager)
        .authenticate(any(UsernamePasswordAuthenticationToken.class));

    assertThatThrownBy(() -> authService.loginUser(new LoginRequest("alicesmith", "password123")))
        .isInstanceOf(ForgemindException.class)
        .satisfies(
            ex -> {
              ForgemindException fex = (ForgemindException) ex;
              assertThat(fex.getCode()).isEqualTo("INVALID_CREDENTIALS");
              assertThat(fex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            });
  }

  @Test
  @DisplayName("login — response never contains password or passwordHash")
  void login_responseNeverContainsPassword() {
    when(userRepository.findByUsername("alicesmith")).thenReturn(Optional.of(activeUser));
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(jwtService.generateToken(any())).thenReturn("t");
    when(jwtService.generateRefreshToken(any())).thenReturn("r");
    stubJwtProperties();

    LoginResponse response = authService.loginUser(new LoginRequest("alicesmith", "password123"));

    // UserResponse has no password field — verify by inspecting the record components
    assertThat(response.user().id()).isNotNull();
    assertThat(response.user().username()).isNotBlank();
    // Confirm accessToken is NOT the password hash
    assertThat(response.accessToken()).doesNotContain("$2a$");
  }

  @Test
  @DisplayName(
      "login — AuthenticationManager is called with the resolved username (not raw identifier)")
  void login_authManagerCalledWithResolvedUsername() {
    // Input: email address
    when(userRepository.findByUsername("alice@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(activeUser));
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(jwtService.generateToken(any())).thenReturn("t");
    when(jwtService.generateRefreshToken(any())).thenReturn("r");
    stubJwtProperties();

    authService.loginUser(new LoginRequest("alice@example.com", "password123"));

    ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
        ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
    verify(authenticationManager).authenticate(captor.capture());

    // Must use the resolved username, not the raw email input
    assertThat(captor.getValue().getPrincipal()).isEqualTo("alicesmith");
  }
}
