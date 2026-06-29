package com.forgemind.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.modules.auth.dto.UpdateUserRequest;
import com.forgemind.modules.auth.dto.UserResponse;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.repository.UserRepository;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UserRepository userRepository;

  @Mock private CurrentUserProvider currentUserProvider;

  @InjectMocks private UserServiceImpl userService;

  private User user;
  private final Long userId = 1L;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(userId);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setFirstName("John");
    user.setLastName("Doe");
  }

  @Test
  @DisplayName("getCurrentUser - Success")
  void getCurrentUser_Success() {
    when(currentUserProvider.getCurrentUser()).thenReturn(user);

    UserResponse result = userService.getCurrentUser();

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(userId);
    assertThat(result.username()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("getUserById - Success")
  void getUserById_Success() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    UserResponse result = userService.getUserById(userId);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(userId);
  }

  @Test
  @DisplayName("getUserById - Not Found")
  void getUserById_NotFound() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getUserById(userId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("updateProfile - Success")
  void updateProfile_Success() {
    UpdateUserRequest request = new UpdateUserRequest("Jane", "Smith");
    when(currentUserProvider.getCurrentUser()).thenReturn(user);
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

    UserResponse result = userService.updateProfile(request);

    assertThat(result).isNotNull();
    assertThat(result.firstName()).isEqualTo("Jane");
    assertThat(result.lastName()).isEqualTo("Smith");
    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("deactivateAccount - Success")
  void deactivateAccount_Success() {
    when(currentUserProvider.getCurrentUser()).thenReturn(user);
    when(userRepository.save(any(User.class))).thenReturn(user);

    userService.deactivateAccount();

    assertThat(user.isEnabled()).isFalse();
    verify(userRepository).save(user);
  }
}
