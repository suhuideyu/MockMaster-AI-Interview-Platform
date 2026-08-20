package com.mockmaster.backend.service.impl;

import com.mockmaster.backend.common.BusinessException;
import com.mockmaster.backend.dto.AuthRequest;
import com.mockmaster.backend.dto.AuthResponse;
import com.mockmaster.backend.dto.UserProfile;
import com.mockmaster.backend.entity.User;
import com.mockmaster.backend.security.JwtTokenUtil;
import com.mockmaster.backend.service.AuthService;
import com.mockmaster.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public AuthResponse register(AuthRequest request) {
        if (userService.getByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        userService.save(user);

        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(
                token,
                new UserProfile(
                        user.getId(),
                        user.getUsername(),
                        user.getPhone(),
                        user.getEmail(),
                        user.getAvatar(),
                        user.getCreateTime() == null ? null : user.getCreateTime().toString()
                )
        );
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userService.getByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(
                token,
                new UserProfile(
                        user.getId(),
                        user.getUsername(),
                        user.getPhone(),
                        user.getEmail(),
                        user.getAvatar(),
                        user.getCreateTime() == null ? null : user.getCreateTime().toString()
                )
        );
    }
}
