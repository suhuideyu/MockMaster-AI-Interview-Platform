package com.mockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualHumanSessionResponse {
    private String sid;
    private String server;
    private String auth;
    private String appid;
    private String userId;
    private String roomId;
    private String timeStr;
    private Boolean enabled;
}