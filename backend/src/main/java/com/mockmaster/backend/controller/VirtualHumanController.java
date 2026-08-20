package com.mockmaster.backend.controller;

import com.mockmaster.backend.common.ApiResponse;
import com.mockmaster.backend.common.BusinessException;
import com.mockmaster.backend.config.VirtualHumanProperties;
import com.mockmaster.backend.dto.VirtualHumanSessionResponse;
import com.mockmaster.backend.service.VirtualHumanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/virtual-human")
@RequiredArgsConstructor
public class VirtualHumanController {

    private final VirtualHumanProperties virtualHumanProperties;
    private final VirtualHumanService virtualHumanService;

    @GetMapping("/session")
    public ApiResponse<VirtualHumanSessionResponse> session(
            @RequestParam(required = false) String userId) {

        if (!virtualHumanProperties.isEnabled()) {
            return ApiResponse.success(new VirtualHumanSessionResponse(
                    null, null, null, null, null, null, null, false));
        }

        if (isBlank(virtualHumanProperties.getServiceId())
                || isBlank(virtualHumanProperties.getAppId())
                || isBlank(virtualHumanProperties.getApiKey())
                || isBlank(virtualHumanProperties.getApiSecret())) {
            throw new BusinessException("请先在 application.yml 中补全 serviceId、appId、apiKey、apiSecret");
        }

        VirtualHumanSessionResponse sessionResponse = virtualHumanService.getSessionParams(userId);
        
        return ApiResponse.success(sessionResponse);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
