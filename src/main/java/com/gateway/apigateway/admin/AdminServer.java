package com.gateway.apigateway.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(
    scanBasePackages = {
        "com.gateway.apigateway.admin",
        "com.gateway.apigateway.controller",
        "com.gateway.apigateway.repo",
        "com.gateway.apigateway.model"
    }
)
@PropertySource("classpath:application-admin.properties")
public class AdminServer {
    public static void main(String[] args) {
        SpringApplication.run(AdminServer.class, args);
    }
}
