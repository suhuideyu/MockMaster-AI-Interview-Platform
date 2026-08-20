package com.mockmaster.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mockmaster.virtual-human")
public class VirtualHumanProperties {
    private boolean enabled;
    private String serviceId;
    private String appId;
    private String apiKey;
    private String apiSecret;
    private String apiUrl;
}
