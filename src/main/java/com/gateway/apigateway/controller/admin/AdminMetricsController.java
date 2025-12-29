package com.gateway.apigateway.controller.admin;

import com.gateway.apigateway.metrics.GatewayMetrics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/metrics")
public class AdminMetricsController {

    private final GatewayMetrics metrics;

    public AdminMetricsController(GatewayMetrics metrics) {
        this.metrics = metrics;
    }

    @GetMapping
    public Map<String, Object> metrics() {
        return metrics.snapshot();
    }
}
