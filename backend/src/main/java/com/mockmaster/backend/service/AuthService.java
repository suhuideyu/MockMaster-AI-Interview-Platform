package com.mockmaster.backend.service;

import com.mockmaster.backend.dto.AuthRequest;
import com.mockmaster.backend.dto.AuthResponse;

public interface AuthService {
    AuthResponse register(AuthRequest request);

    AuthResponse login(AuthRequest request);
}
