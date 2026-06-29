package com.forgemind.modules.auth.dto;

import com.forgemind.modules.auth.entity.User;
import java.time.Instant;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;

public record UserResponse(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    List<String> role,
    Instant createdAt) {
  public static UserResponse fromUser(User user) {
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
        user.getCreatedAt());
  }
}
