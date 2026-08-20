package com.mockmaster.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadRealPath = "file:" + System.getProperty("user.dir") + "/upload/";
        
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(uploadRealPath);
                
        log.info("头像映射配置完成: /upload/** -> " + uploadRealPath);
    }
}