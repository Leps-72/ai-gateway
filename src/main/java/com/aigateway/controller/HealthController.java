package com.aigateway.controller;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Service health check")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Check gateway health")
    public Map<String, String> health() {
        return Map.of(
                "status", "ok",
                "service", "AI Gateway"
        );
    }
}
