package com.forgemind.modules.auth.dto;

import com.forgemind.modules.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        List<String> role,
        Instant createdAt
) {
    public static UserResponse fromUser(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList(),
                user.getCreatedAt()
        );
    }
}
