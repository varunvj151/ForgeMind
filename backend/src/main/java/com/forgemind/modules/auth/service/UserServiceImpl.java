package com.forgemind.modules.auth.service;

import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.modules.auth.dto.UpdateUserRequest;
import com.forgemind.modules.auth.dto.UserResponse;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.repository.UserRepository;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final CurrentUserProvider currentUserProvider;

  @Override
  @Transactional(readOnly = true)
  public UserResponse getCurrentUser() {
    User user = currentUserProvider.getCurrentUser();
    return UserResponse.fromUser(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse getUserById(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "USER_NOT_FOUND", "User not found with id: " + id));
    return UserResponse.fromUser(user);
  }

  @Override
  @Transactional
  public UserResponse updateProfile(UpdateUserRequest request) {
    User user = currentUserProvider.getCurrentUser();

    user.setFirstName(request.firstName().trim());
    user.setLastName(request.lastName().trim());

    User saved = userRepository.save(user);
    log.info("User {} updated their profile", user.getUsername());

    return UserResponse.fromUser(saved);
  }

  @Override
  @Transactional
  public void deactivateAccount() {
    User user = currentUserProvider.getCurrentUser();
    user.setEnabled(false);
    userRepository.save(user);
    log.info("User {} deactivated their account", user.getUsername());
  }
}
