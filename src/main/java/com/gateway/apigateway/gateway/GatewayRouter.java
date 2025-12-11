package com.gateway.apigateway.gateway;

import com.gateway.apigateway.model.Route;
import com.gateway.apigateway.repo.RouteRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@Component
public class GatewayRouter {

    private final RouteRepository repo;

    public GatewayRouter(RouteRepository repo) {
        this.repo = repo;
    }

    public Mono<Route> match(org.springframework.http.server.reactive.ServerHttpRequest request) {
        String path = request.getPath().value(); // e.g., /api/boa
        return Mono.fromCallable(() -> repo.findAll().stream()
                .filter(r -> path.equals(r.getPath())) // exact match
                .findFirst()
                .orElse(null)).flatMap(r -> r == null ? Mono.empty() : Mono.just(r));
    }
}
