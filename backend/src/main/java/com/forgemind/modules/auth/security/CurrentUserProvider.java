package com.forgemind.modules.auth.security;

import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility for retrieving the currently authenticated user.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    /**
     * Gets the currently authenticated User entity.
     * 
     * @return the authenticated User
     * @throws ResourceNotFoundException if no user is authenticated or the user no longer exists
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResourceNotFoundException("UNAUTHORIZED", "No authenticated user found in context.");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Authenticated user not found in database."));
    }
}
