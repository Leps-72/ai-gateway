package com.aigateway.dto;

public class UsageResponse {

    private long requests;
    private long tokens;
    private double averageLatencyMs;
    private double errorRate;

    public UsageResponse(long requests, long tokens, double averageLatencyMs, double errorRate) {
        this.requests = requests;
        this.tokens = tokens;
        this.averageLatencyMs = averageLatencyMs;
        this.errorRate = errorRate;
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
}
