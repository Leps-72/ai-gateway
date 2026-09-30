package com.aigateway.dto;

public class AiChatResponse {

    private String response;
    private String provider;
    private String model;
    private String status;

    public AiChatResponse() {
    }

    public AiChatResponse(String response, String provider, String model, String status) {
        this.response = response;
        this.provider = provider;
        this.model = model;
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
