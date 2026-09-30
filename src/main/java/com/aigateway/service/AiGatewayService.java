package com.aigateway.service;

import com.aigateway.provider.AiProvider;
import com.aigateway.provider.AiProviderResult;
import com.aigateway.provider.AnalyzeProviderResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AiGatewayService {

    private final AiProvider aiProvider;

    public AiGatewayService(@Qualifier("geminiProvider") AiProvider aiProvider) {
        this.aiProvider = aiProvider;
    }

    public AiProviderResult chat(String message) {
        return aiProvider.chat(message);
    }

    public AnalyzeProviderResult analyze(String text) {
        return aiProvider.analyze(text);
    }

    public String getProviderName() {
        return aiProvider.getProviderName();
    }

    public String getModelName() {
        return aiProvider.getModelName();
    }
}
