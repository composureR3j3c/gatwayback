package com.gateway.apigateway.gateway;

import com.gateway.apigateway.model.Route;
import com.gateway.apigateway.util.GatewayAccessLogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;
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

    public Mono<ServerResponse> handle(ServerWebExchange exchange) {
        System.err.println(exchange.getRequest().getURI().getPath() + " -> handling request");
        return router.match(exchange.getRequest())
                .flatMap(route -> forward(route, exchange))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    private Mono<ServerResponse> forward(Route route, ServerWebExchange exchange) {

        HttpMethod method = exchange.getRequest().getMethod();
        String path = exchange.getRequest().getURI().getPath();
        String relativePath = path.substring(route.getPath().length());
        String target = route.getUpstream() + relativePath;

        return client.method(method)
                .uri(target)
                .headers(headers -> {
                    headers.addAll(exchange.getRequest().getHeaders());
                    headers.remove("Host");
                    headers.remove("Content-Length");
                    headers.remove("Transfer-Encoding");
                    headers.remove("Accept-Encoding");
                    headers.remove("Connection");
                    headers.remove("Keep-Alive");
                    headers.remove("Proxy-Connection");
                    headers.remove("Upgrade");
                    headers.remove("Sec-Fetch-Site");
                    headers.remove("Sec-Fetch-Mode");
                })
                .exchangeToMono(resp -> resp.toEntity(byte[].class)
                        .flatMap(entity -> {
                            // Set upstream status
                            exchange.getResponse().setStatusCode(entity.getStatusCode());
                            // Set upstream headers
                            exchange.getResponse().getHeaders().addAll(entity.getHeaders());
                            System.out.println("Forwarded to: " + target + " with status: " + entity.getStatusCode());

                            // Log access

                            String latencyHeader = entity.getHeaders().getFirst("X-Response-Time");
                            long latency = latencyHeader != null ? Long.parseLong(latencyHeader) : 0L;
                            String headers;
                            String responseBody;
                            try {
                                ObjectMapper mapper = new ObjectMapper();
                                headers = mapper.writeValueAsString(exchange.getRequest().getHeaders().toSingleValueMap());
                                responseBody = mapper.writeValueAsString(entity.getBody()).toString();
                            } catch (JsonProcessingException e) {
                                headers = exchange.getRequest().getHeaders().toString();
                                responseBody= new String(entity.getBody());
                            }
                  

                            GatewayAccessLogUtil.log(
                                    method.name(),
                                    path,
                                    headers,
                                    target,
                                    entity.getStatusCode().value(),
                                    responseBody,
                                    latency);

                            // Write upstream body
                            return exchange.getResponse()
                                    .writeWith(Mono.just(
                                            exchange.getResponse().bufferFactory()
                                                    .wrap(entity.getBody())));
                        }))
                .then(Mono.empty()); // Do not override status with 200
    }
}
