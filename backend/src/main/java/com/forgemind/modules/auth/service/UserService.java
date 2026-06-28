package com.forgemind.modules.auth.service;

import com.forgemind.modules.auth.dto.UpdateUserRequest;
import com.forgemind.modules.auth.dto.UserResponse;

public interface UserService {

    /**
     * Gets the profile of the currently authenticated user.
     */
    UserResponse getCurrentUser();

    /**
     * Gets a user profile by ID.
     */
    UserResponse getUserById(Long id);

    /**
     * Updates the profile of the currently authenticated user.
     */
    UserResponse updateProfile(UpdateUserRequest request);

    /**
     * Disables (soft deletes) the currently authenticated user's account.
     */
    void deactivateAccount();
}
