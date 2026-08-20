package com.mockmaster.backend.security;

public class LoginUser {

    private final Long userId;
    private final String username;

    public LoginUser(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long userId() {
        return userId;
    }

    public String username() {
        return username;
    }
}
