package com.aigateway.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregated AI usage and estimated cost")
public class UsageResponse {

    private long requests;
    private long tokens;
    private double averageLatencyMs;
    private double errorRate;
    @Schema(description = "Estimated total cost in USD; null when estimation is unavailable", nullable = true)
    private BigDecimal estimatedCostUsd;
    @Schema(description = "Whether all stored requests could be priced")
    private boolean costEstimationAvailable;

    public UsageResponse(
            long requests,
            long tokens,
            double averageLatencyMs,
            double errorRate,
            BigDecimal estimatedCostUsd,
            boolean costEstimationAvailable
    ) {
        this.requests = requests;
        this.tokens = tokens;
        this.averageLatencyMs = averageLatencyMs;
        this.errorRate = errorRate;
        this.estimatedCostUsd = estimatedCostUsd;
        this.costEstimationAvailable = costEstimationAvailable;
    }

    public long getRequests() {
        return requests;
    }

    public void setRequests(long requests) {
        this.requests = requests;
    }

    public long getTokens() {
        return tokens;
    }

    public void setTokens(long tokens) {
        this.tokens = tokens;
    }

    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public void setAverageLatencyMs(double averageLatencyMs) {
        this.averageLatencyMs = averageLatencyMs;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public BigDecimal getEstimatedCostUsd() {
        return estimatedCostUsd;
    }

    public void setEstimatedCostUsd(BigDecimal estimatedCostUsd) {
        this.estimatedCostUsd = estimatedCostUsd;
    }

    public boolean isCostEstimationAvailable() {
        return costEstimationAvailable;
    }

    public void setCostEstimationAvailable(boolean costEstimationAvailable) {
        this.costEstimationAvailable = costEstimationAvailable;
    }
}
