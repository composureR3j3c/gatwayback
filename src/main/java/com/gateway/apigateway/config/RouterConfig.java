package com.gateway.apigateway.config;

import com.gateway.apigateway.gateway.GatewayHandler;

import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    private final GatewayHandler handler;

    public RouterConfig(GatewayHandler handler) {
        this.handler = handler;
    }

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
                .path("/api", builder -> builder
                        .GET("/**", this::forwardToHandler)
                        .POST("/**", this::forwardToHandler)
                        .PUT("/**", this::forwardToHandler)
                        .DELETE("/**", this::forwardToHandler))
                .build();
    }

    private Mono<ServerResponse> forwardToHandler(
            org.springframework.web.reactive.function.server.ServerRequest request) {
        return handler.handle(request.exchange());
    }
}
