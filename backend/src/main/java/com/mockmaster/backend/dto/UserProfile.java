package com.mockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfile {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String avatar;
    private String createTime;
}
