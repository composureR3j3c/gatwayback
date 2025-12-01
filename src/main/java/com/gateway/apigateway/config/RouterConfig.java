package com.gateway.apigateway.config;

import com.gateway.apigateway.gateway.GatewayHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class RouterConfig {

    private final GatewayHandler handler;


    public RouterConfig(GatewayHandler handler) {
        this.handler = handler;
    }
    @Bean
    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions
                // .route(RequestPredicates.path("/admin/**"), request -> ServerResponse.notFound().build())
                .route(RequestPredicates.path("/**").and(req -> !req.path().startsWith("/admin") &&
                !req.path().startsWith("/actuator")),
                        req -> handler.handle(req.exchange()).then(ServerResponse.ok().build()));
    }
}
