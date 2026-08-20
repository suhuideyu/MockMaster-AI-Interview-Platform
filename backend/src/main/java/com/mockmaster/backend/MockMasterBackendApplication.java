package com.mockmaster.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@MapperScan("com.mockmaster.backend.mapper")
@SpringBootApplication
public class MockMasterBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockMasterBackendApplication.class, args);
    }
}

