package com.mockmaster.backend.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.mockmaster.backend.common.BusinessException;
import com.mockmaster.backend.config.VirtualHumanProperties;
import com.mockmaster.backend.dto.VirtualHumanSessionResponse;
import com.mockmaster.backend.util.IflytekAuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VirtualHumanService {

    private final VirtualHumanProperties properties;

    public VirtualHumanSessionResponse getSessionParams(String userId) {
        try {
            Map<String, String> map = getIflytekSessionParams(userId);

            return new VirtualHumanSessionResponse(
                    map.get("appId"),
                    map.get("serviceId"),
                    map.get("userId"),
                    map.get("sessionId"),
                    map.get("token"),
                    map.get("url"),
                    null,
                    true
            );
        } catch (Exception e) {
            log.error("获取虚拟人会话参数失败", e);
            throw new BusinessException("获取虚拟人会话参数失败：" + e.getMessage());
        }
    }

    private Map<String, String> getIflytekSessionParams(String userId) throws Exception {
        if (properties.getApiUrl() == null || properties.getApiUrl().isBlank()) {
            throw new BusinessException("讯飞虚拟人 API URL 未配置，请检查 application.yml 中的 mockmaster.virtual-human.api-url");
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BusinessException("讯飞虚拟人 API Key 未配置");
        }
        if (properties.getApiSecret() == null || properties.getApiSecret().isBlank()) {
            throw new BusinessException("讯飞虚拟人 API Secret 未配置");
        }

        long currentTime = System.currentTimeMillis();
        String timeStr = String.valueOf(currentTime / 1000);
        String requestId = IdUtil.simpleUUID();

        if (userId == null || userId.isEmpty()) {
            userId = "user_" + requestId;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("service_id", properties.getServiceId());
        body.put("app_id", properties.getAppId());
        body.put("user_id", userId);
        body.put("request_id", requestId);
        body.put("timestamp", timeStr);

        String jsonBody = JSONUtil.toJsonStr(body);
        byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);

        String apiUrl = properties.getApiUrl();

        log.info("调用讯飞接口URL: {}", apiUrl);
        log.info("请求参数: {}", jsonBody);

        Map<String, String> headers = IflytekAuthUtil.generateAuthHeaders(
                apiUrl,
                properties.getApiKey(),
                properties.getApiSecret(),
                "POST",
                bodyBytes
        );

        String response = HttpRequest.post(apiUrl)
                .headerMap(headers, false)
                .contentType("application/json;charset=UTF-8")
                .body(jsonBody)
                .timeout(30000)
                .execute()
                .body();

        log.info("讯飞接口返回: {}", response);

        if (response == null || response.isBlank()) {
            throw new BusinessException("讯飞接口返回空，请检查密钥/服务ID是否正确");
        }

        return parseIflytekResponse(response);
    }

    private Map<String, String> parseIflytekResponse(String response) {
        Map<String, Object> resMap = JSONUtil.toBean(response, Map.class);

        String code = String.valueOf(resMap.get("code"));
        String desc = String.valueOf(resMap.get("desc"));

        log.info("讯飞返回code={}, desc={}", code, desc);

        if (!"0".equals(code) && !"200".equals(code)) {
            throw new BusinessException("虚拟人接口错误：" + desc + "（错误码：" + code + "）");
        }

        Object dataObj = resMap.get("data");
        if (dataObj == null) {
            throw new BusinessException("讯飞接口返回数据为空：" + response);
        }

        Map<String, Object> data = (Map<String, Object>) dataObj;

        Map<String, String> result = new HashMap<>();
        result.put("appId", properties.getAppId());
        result.put("serviceId", properties.getServiceId());
        result.put("userId", String.valueOf(data.get("user_id")));
        result.put("sessionId", String.valueOf(data.get("session_id")));
        result.put("token", String.valueOf(data.get("token")));
        result.put("url", String.valueOf(data.get("url")));

        return result;
    }
}