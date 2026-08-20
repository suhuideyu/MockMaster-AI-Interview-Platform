package com.mockmaster.backend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String phone;
    private String email;
    private String avatar;
}
