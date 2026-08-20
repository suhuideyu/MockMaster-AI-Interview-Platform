package com.mockmaster.backend.service;

import com.mockmaster.backend.dto.UpdateProfileRequest;
import com.mockmaster.backend.dto.UserProfile;
import com.mockmaster.backend.entity.User;

public interface UserService {
    User getById(Long userId);

    User getByUsername(String username);

    void save(User user);

    UserProfile getProfile(Long userId);

    UserProfile updateProfile(Long userId, UpdateProfileRequest request);
}
