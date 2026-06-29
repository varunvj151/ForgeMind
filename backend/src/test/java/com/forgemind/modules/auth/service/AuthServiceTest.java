package com.forgemind.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.forgemind.common.exception.ForgemindException;
import com.forgemind.modules.auth.dto.RegisterRequest;
import com.forgemind.modules.auth.dto.UserResponse;
import com.forgemind.modules.auth.entity.Role;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.repository.RoleRepository;
import com.forgemind.modules.auth.repository.UserRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private RoleRepository roleRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AuthService authService;

  private RegisterRequest validRequest;
  private Role userRole;

  @BeforeEach
  void setUp() {
    validRequest =
        new RegisterRequest("John", "Doe", "johndoe", "john@example.com", "securepassword123");

    userRole = new Role();
    userRole.setId(1L);
    userRole.setName("ROLE_USER");
  }

  @Test
  @DisplayName("register - success: returns UserResponse with id, username, email, names, and role")
  void register_success() {
    when(userRepository.existsByUsername("johndoe")).thenReturn(false);
    when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
    when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
    when(passwordEncoder.encode("securepassword123")).thenReturn("hashed-password");

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    User savedUser =
        User.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .username("johndoe")
            .email("john@example.com")
            .passwordHash("hashed-password")
            .roles(roles)
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    UserResponse response = authService.registerUser(validRequest);

    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.username()).isEqualTo("johndoe");
    assertThat(response.email()).isEqualTo("john@example.com");
    assertThat(response.firstName()).isEqualTo("John");
    assertThat(response.lastName()).isEqualTo("Doe");
    assertThat(response.role()).contains("ROLE_USER");
    assertThat(response.createdAt()).isNotNull();

    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("register - conflict: username already taken throws 409")
  void register_usernameExists_throws409() {
    when(userRepository.existsByUsername("johndoe")).thenReturn(true);

    assertThatThrownBy(() -> authService.registerUser(validRequest))
        .isInstanceOf(ForgemindException.class)
        .satisfies(
            ex -> {
              ForgemindException fex = (ForgemindException) ex;
              assertThat(fex.getCode()).isEqualTo("USERNAME_EXISTS");
              assertThat(fex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
            });

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("register - conflict: email already taken throws 409")
  void register_emailExists_throws409() {
    when(userRepository.existsByUsername("johndoe")).thenReturn(false);
    when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

    assertThatThrownBy(() -> authService.registerUser(validRequest))
        .isInstanceOf(ForgemindException.class)
        .satisfies(
            ex -> {
              ForgemindException fex = (ForgemindException) ex;
              assertThat(fex.getCode()).isEqualTo("EMAIL_EXISTS");
              assertThat(fex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
            });

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("register - trims whitespace and lowercases email before saving")
  void register_trimsAndLowercasesInput() {
    RegisterRequest dirtyRequest =
        new RegisterRequest(
            "  John  ", " Doe ", "  johndoe  ", "  JOHN@EXAMPLE.COM  ", "securepassword123");

    when(userRepository.existsByUsername("johndoe")).thenReturn(false);
    when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
    when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
    when(passwordEncoder.encode(anyString())).thenReturn("hashed");

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    User savedUser =
        User.builder()
            .id(2L)
            .firstName("John")
            .lastName("Doe")
            .username("johndoe")
            .email("john@example.com")
            .passwordHash("hashed")
            .roles(roles)
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    authService.registerUser(dirtyRequest);

    // verify we checked the trimmed/lowercased values
    verify(userRepository).existsByUsername("johndoe");
    verify(userRepository).existsByEmail("john@example.com");
  }

  @Test
  @DisplayName("register - password is never stored in plain text")
  void register_passwordIsHashed() {
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
    when(passwordEncoder.encode("securepassword123")).thenReturn("$2a$12$hashed");

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    User savedUser =
        User.builder()
            .id(3L)
            .firstName("John")
            .lastName("Doe")
            .username("johndoe")
            .email("john@example.com")
            .passwordHash("$2a$12$hashed")
            .roles(roles)
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    authService.registerUser(validRequest);

    verify(passwordEncoder).encode("securepassword123");
    verify(userRepository).save(argThat(u -> u.getPasswordHash().equals("$2a$12$hashed")));
  }
}
