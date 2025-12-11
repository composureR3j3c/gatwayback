package com.gateway.apigateway.config;

import com.gateway.apigateway.gateway.GatewayHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class RouterConfig {

    private final GatewayHandler handler;

    public RouterConfig(GatewayHandler handler) {
        this.handler = handler;
    }

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
                // API routes go through gateway
                .path("/api", builder -> builder
                        .GET("/{segment:.*}", this::forwardToHandler)
                        .POST("/{segment:.*}", this::forwardToHandler)
                        .PUT("/{segment:.*}", this::forwardToHandler)
                        .DELETE("/{segment:.*}", this::forwardToHandler))
                .build();
    }
    private Mono<ServerResponse> forwardToHandler(ServerRequest request) {
        ServerWebExchange exchange = request.exchange();
       return handler.handle(exchange).then(ServerResponse.ok().build());
    }
}
