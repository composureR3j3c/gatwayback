package com.gateway.apigateway.gateway;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.apigateway.metrics.GatewayMetrics;
import com.gateway.apigateway.model.Route;
import com.gateway.apigateway.util.GatewayAccessLogUtil;
import com.gateway.apigateway.util.GatewayRequestUtil;

import reactor.core.publisher.Mono;

@Component
public class GatewayHandler {

    private final GatewayRouter router;
    private final WebClient client;
    private final GatewayMetrics metrics;

    public GatewayHandler(GatewayRouter router, GatewayMetrics metrics) {
        this.router = router;
        this.metrics = metrics;
        this.client = WebClient.builder().build();
    }

    public Mono<ServerResponse> handle(ServerWebExchange exchange) {
        System.err.println(exchange.getRequest().getURI().getPath() + " -> handling request");
        metrics.onRequestStart();
        long start = System.currentTimeMillis();
        return router.match(exchange.getRequest())
                .flatMap(route -> forward(route, exchange, start))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    private Mono<ServerResponse> forward(Route route, ServerWebExchange exchange, long start) {
        HttpMethod method = exchange.getRequest().getMethod();
        String path = exchange.getRequest().getURI().getPath();
        String relativePath = path.substring(route.getPath().length());
        String target = route.getUpstream() + relativePath;
        long startTime = System.nanoTime();

        String requestId = GatewayRequestUtil.getRequestId(exchange.getRequest());
        String clientIp = GatewayRequestUtil.getClientIp(exchange.getRequest());

        final String requestHeadersJson = headersToJson(exchange);

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
                            byte[] bodyBytes = entity.getBody();
                            String latencyHeader = entity.getHeaders().getFirst("X-Response-Time");
                            long latency = latencyHeader != null ? Long.parseLong(latencyHeader) : 1L;
                            String responseBody;
                            MediaType ct = exchange.getRequest().getHeaders().getContentType();
                            System.err.println("Content-Type: " + ct);
                            Mono<String> requestBodyMono = ct != null
                                    ? extractRequestBody(exchange)
                                    : Mono.just("");
                            final String headers = requestHeadersJson;

                            // Compose write and logging reactively (no blocking)
                            return requestBodyMono.flatMap(requestBody -> {
                                System.out.println("Request Body: " + requestBody);
                                String respBody = bodyBytes != null ? new String(bodyBytes, StandardCharsets.UTF_8)
                                        : "";
                                metrics.onRequestEnd("NO_MATCH", 404,
                                        System.currentTimeMillis() - start);
                                return exchange.getResponse()
                                        .writeWith(Mono.just(
                                                exchange.getResponse().bufferFactory()
                                                        .wrap(entity.getBody())))
                                        .doFinally(signal -> {
                                            long latencyMs = (System.nanoTime() - startTime) / 1_000_000;
                                            GatewayAccessLogUtil.log(
                                                    clientIp,
                                                    requestId,
                                                    requestBody,
                                                    method.name(),
                                                    path,
                                                    headers != null ? headers : "",
                                                    target,
                                                    entity.getStatusCode().value(),
                                                    respBody,
                                                    latencyMs);

                                        });
                            });
                        }))
                .then(Mono.empty()); // Do not override status with 200
    }

    private static String headersToJson(ServerWebExchange exchange) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(exchange.getRequest().getHeaders().toSingleValueMap());
        } catch (JsonProcessingException e) {
            return exchange.getRequest().getHeaders().toString();
        }
    }

    private static Mono<String> extractRequestBody(ServerWebExchange exchange) {
        return exchange.getRequest().getBody()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .reduce((s1, s2) -> s1 + s2)
                .defaultIfEmpty("");
    }
}
