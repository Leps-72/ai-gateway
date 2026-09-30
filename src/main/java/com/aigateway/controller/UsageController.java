package com.aigateway.controller;

import com.aigateway.dto.UsageResponse;
import com.aigateway.service.UsageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/usage")
    public UsageResponse getUsage() {
        return usageService.getUsage();
    }
}
