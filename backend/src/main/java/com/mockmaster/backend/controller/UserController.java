package com.mockmaster.backend.controller;

import com.mockmaster.backend.common.ApiResponse;
import com.mockmaster.backend.dto.UpdateProfileRequest;
import com.mockmaster.backend.dto.UserProfile;
import com.mockmaster.backend.security.LoginUser;
import com.mockmaster.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfile> me(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return ApiResponse.success(userService.getProfile(loginUser.userId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserProfile> updateMe(@RequestBody UpdateProfileRequest request,
                                              Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return ApiResponse.success(userService.updateProfile(loginUser.userId(), request));
    }
}
