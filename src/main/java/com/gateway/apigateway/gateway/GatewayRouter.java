package com.gateway.apigateway.gateway;

import com.gateway.apigateway.model.Route;
import com.gateway.apigateway.repo.RouteRepository;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class GatewayRouter {

    private final RouteRepository repo;

    public GatewayRouter(RouteRepository repo) {
        this.repo = repo;
    }

    public Mono<Route> match(ServerHttpRequest request) {
        String path = request.getPath().value();
        System.err.println("Matching path: " + path);
        return Mono.fromCallable(() -> repo.findAll().stream()
                .sorted((a, b) -> Integer.compare(b.getPath().length(), a.getPath().length())) // longest first
                .filter(r -> path.startsWith(r.getPath()))
                .findFirst()
                .orElse(null)).flatMap(r -> r == null ? Mono.empty() : Mono.just(r));
    }
}
