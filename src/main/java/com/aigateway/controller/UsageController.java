package com.aigateway.controller;

import com.aigateway.dto.UsageResponse;
import com.aigateway.service.UsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Usage", description = "Aggregated AI usage metrics")
@SecurityRequirement(name = "bearerAuth")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/usage")
    @Operation(summary = "Get aggregated usage metrics and configured cost estimation")
    public UsageResponse getUsage() {
        return usageService.getUsage();
    }
}
