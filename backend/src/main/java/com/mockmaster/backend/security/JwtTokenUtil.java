package com.mockmaster.backend.security;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenUtil {

    @Value("${mockmaster.jwt-secret:mockmaster-jwt-secret-key}")
    private String jwtSecret;

    @Value("${mockmaster.jwt-expire-hours:24}")
    private Integer expireHours;

    public String generateToken(Long userId, String username) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("exp", DateUtil.offsetHour(new Date(), expireHours).getTime() / 1000);
        return JWTUtil.createToken(payload, jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Long parseUserId(String token) {
        if (StrUtil.isBlank(token) || token.chars().filter(ch -> ch == '.').count() != 2) {
            return null;
        }
        try {
            if (!JWTUtil.verify(token, jwtSecret.getBytes(StandardCharsets.UTF_8))) {
                return null;
            }
            JWT jwt = JWTUtil.parseToken(token);
            Object userId = jwt.getPayload("userId");
            return userId == null ? null : Long.parseLong(userId.toString());
        } catch (JWTException | IllegalArgumentException ex) {
            return null;
        }
    }
}
