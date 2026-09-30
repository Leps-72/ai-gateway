package com.aigateway.provider;

public interface AiProvider {

    String getProviderName();

    String getModelName();

    AiProviderResult chat(String message);

    AnalyzeProviderResult analyze(String text);
}
