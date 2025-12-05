package com.gateway.apigateway.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
        "com.gateway.apigateway.admin",
        "com.gateway.apigateway.controller",
        "com.gateway.apigateway.repo",
        "com.gateway.apigateway.model"
    }
)
public class AdminServer {
    public static void main(String[] args) {
        SpringApplication.run(AdminServer.class, args);
    }
}
