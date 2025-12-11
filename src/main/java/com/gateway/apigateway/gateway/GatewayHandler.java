package com.gateway.apigateway.gateway;

import com.gateway.apigateway.model.Route;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayHandler {

    private final GatewayRouter router;
    private final WebClient client;

    public GatewayHandler(GatewayRouter router) {
        this.router = router;
        this.client = WebClient.builder().build();
    }

    public Mono<Void> handle(ServerWebExchange exchange) {
        return router.match(exchange.getRequest())
                .flatMap(route -> forward(route, exchange))
                .switchIfEmpty(Mono.error(new RuntimeException(
                        "No route matched for path: " + exchange.getRequest().getPath().value()
                )));
    }

    private Mono<Void> forward(Route route, ServerWebExchange exchange) {
        HttpMethod method = exchange.getRequest().getMethod();
        String incomingPath = exchange.getRequest().getURI().getPath();

        // Remove route prefix
        String relativePath = incomingPath.substring(route.getPath().length());
        String target = route.getUpstream() + relativePath;

        return client.method(method)
                .uri(target)
                .headers(headers -> headers.addAll(exchange.getRequest().getHeaders()))
                .body(exchange.getRequest().getBody(), Object.class)
                .exchangeToMono(resp -> {
                    exchange.getResponse().setStatusCode(resp.statusCode());
                    exchange.getResponse().getHeaders().addAll(resp.headers().asHttpHeaders());
                    return exchange.getResponse().writeWith(
                            resp.bodyToFlux(byte[].class)
                                    .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
                    );
                });
    }
}
