package com.mockmaster.backend.security;

import cn.hutool.core.util.StrUtil;
import com.mockmaster.backend.entity.User;
import com.mockmaster.backend.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(authorization) && authorization.startsWith("Bearer ")) {
            String token = StrUtil.trim(authorization.substring(7));
            Long userId = jwtTokenUtil.parseUserId(token);
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getById(userId);
                if (user != null) {
                    LoginUser loginUser = new LoginUser(user.getId(), user.getUsername());
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(loginUser, null, AuthorityUtils.NO_AUTHORITIES);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
