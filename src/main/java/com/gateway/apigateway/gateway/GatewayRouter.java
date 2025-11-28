package com.gateway.apigateway.gateway;

import com.gateway.apigateway.model.Route;
import com.gateway.apigateway.repo.RouteRepository;
import org.springframework.stereotype.Component;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

@Component
public class GatewayRouter {

    private final RouteRepository repo;

    public GatewayRouter(RouteRepository repo) {
        this.repo = repo;
    }

    public Mono<Route> match(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        return Mono.fromCallable(() ->
            repo.findAll().stream()
                .filter(r -> path.startsWith(r.getPath()))
                .findFirst()
                .orElse(null)
        );
    }
}
