package com.mockmaster.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mockmaster.backend.dto.UpdateProfileRequest;
import com.mockmaster.backend.dto.UserProfile;
import com.mockmaster.backend.entity.User;
import com.mockmaster.backend.mapper.UserMapper;
import com.mockmaster.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("limit 1"));
    }

    @Override
    public void save(User user) {
        userMapper.insert(user);
    }

    @Override
    public UserProfile getProfile(Long userId) {
        User user = getById(userId);
        return new UserProfile(
                user.getId(),
                user.getUsername(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatar(),
                user.getCreateTime() == null ? null : user.getCreateTime().toString()
        );
    }

    @Override
    public UserProfile updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getById(userId);
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);
        return getProfile(userId);
    }
}
