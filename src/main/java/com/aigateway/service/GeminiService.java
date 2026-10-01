package com.aigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.errors.ApiException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.aigateway.exception.AiProviderException;
import com.aigateway.exception.AiTimeoutException;
import com.aigateway.exception.InvalidAiResponseException;
import com.aigateway.provider.AiProvider;
import com.aigateway.provider.AiProviderResult;
import com.aigateway.provider.AnalyzeProviderResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Set;

@Service("geminiProvider")
public class GeminiService implements AiProvider {

    private static final Set<String> RESPONSE_FIELDS =
            Set.of("summary", "sentiment", "category", "priority");
    private static final Set<String> SENTIMENTS = Set.of("positive", "neutral", "negative");
    private static final Set<String> CATEGORIES =
            Set.of("performance", "bug", "feature_request", "usability", "other");
    private static final Set<String> PRIORITIES = Set.of("low", "medium", "high");

    private static final Schema ANALYZE_SCHEMA = Schema.builder()
            .type(Type.Known.OBJECT)
            .properties(Map.of(
                    "summary", Schema.builder().type(Type.Known.STRING).build(),
                    "sentiment", Schema.builder()
                            .type(Type.Known.STRING)
                            .enum_("positive", "neutral", "negative")
                            .build(),
                    "category", Schema.builder()
                            .type(Type.Known.STRING)
                            .enum_("performance", "bug", "feature_request", "usability", "other")
                            .build(),
                    "priority", Schema.builder()
                            .type(Type.Known.STRING)
                            .enum_("low", "medium", "high")
                            .build()
            ))
            .required("summary", "sentiment", "category", "priority")
            .propertyOrdering("summary", "sentiment", "category", "priority")
            .build();

    private static final GenerateContentConfig ANALYZE_CONFIG = GenerateContentConfig.builder()
            .responseMimeType("application/json")
            .responseSchema(ANALYZE_SCHEMA)
            .build();

    private final ObjectMapper objectMapper;
    private final String model;
    private final ContentGenerator contentGenerator;

    @Autowired
    public GeminiService(
            ObjectMapper objectMapper,
            @Value("${ai.gemini.model}") String model,
            @Value("${ai.request-timeout-seconds:10}") int requestTimeoutSeconds,
            @Value("${ai.max-attempts:3}") int maxAttempts,
            @Value("${ai.retry-backoff-ms:500}") long retryBackoffMs
    ) {
        validateConfiguration(requestTimeoutSeconds, maxAttempts, retryBackoffMs);
        String apiKey = System.getenv("GEMINI_API_KEY");
        Client client = apiKey == null || apiKey.isBlank()
                ? null
                : Client.builder()
                        .apiKey(apiKey)
                        .httpOptions(buildHttpOptions(
                                requestTimeoutSeconds,
                                maxAttempts,
                                retryBackoffMs
                        ))
                        .build();
        this.objectMapper = objectMapper;
        this.model = validateModel(model);
        this.contentGenerator = client == null ? null : client.models::generateContent;
    }

    GeminiService(ObjectMapper objectMapper, String model, ContentGenerator contentGenerator) {
        this.objectMapper = objectMapper;
        this.model = validateModel(model);
        this.contentGenerator = contentGenerator;
    }

    @Override
    public AiProviderResult chat(String message) {
        if (contentGenerator == null) {
            throw new AiProviderException("AI provider is not configured.");
        }

        try {
            GenerateContentResponse response = contentGenerator.generate(model, message, null);
            String responseText = response.text();
            if (responseText == null || responseText.isBlank()) {
                throw new InvalidAiResponseException("Gemini returned an empty response.");
            }

            TokenUsage usage = getTokenUsage(response);

            return new AiProviderResult(responseText, usage.inputTokens(), usage.outputTokens());
        } catch (InvalidAiResponseException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw mapProviderException(exception);
        }
    }

    @Override
    public AnalyzeProviderResult analyze(String text) {
        if (contentGenerator == null) {
            throw new AiProviderException("AI provider is not configured.");
        }

        String prompt = "Analyze the following text. Summarize it and classify its sentiment, "
                + "category, and priority according to the provided response schema.\n\nText:\n"
                + text;

        try {
            GenerateContentResponse response = contentGenerator.generate(
                    model,
                    prompt,
                    ANALYZE_CONFIG
            );
            String responseText = response.text();
            AnalyzeContent analyzeContent = parseAnalyzeResponse(responseText);
            String responseJson = objectMapper.writeValueAsString(analyzeContent);
            TokenUsage usage = getTokenUsage(response);

            return new AnalyzeProviderResult(
                    analyzeContent.summary(),
                    analyzeContent.sentiment(),
                    analyzeContent.category(),
                    analyzeContent.priority(),
                    responseJson,
                    usage.inputTokens(),
                    usage.outputTokens()
            );
        } catch (InvalidAiResponseException exception) {
            throw exception;
        } catch (JsonProcessingException exception) {
            throw new InvalidAiResponseException("Gemini response could not be serialized.", exception);
        } catch (RuntimeException exception) {
            throw mapProviderException(exception);
        }
    }

    private AnalyzeContent parseAnalyzeResponse(String responseText) {
        if (responseText == null || responseText.isBlank()) {
            throw new InvalidAiResponseException("Gemini returned an empty analyze response.");
        }

        try {
            JsonNode json = objectMapper.readTree(responseText);
            if (!json.isObject() || json.size() != RESPONSE_FIELDS.size()) {
                throw new InvalidAiResponseException("Gemini returned an invalid analyze response.");
            }
            for (String field : RESPONSE_FIELDS) {
                if (!json.has(field) || !json.get(field).isTextual()) {
                    throw new InvalidAiResponseException("Gemini analyze response is missing a required field.");
                }
            }

            AnalyzeContent result = new AnalyzeContent(
                    json.get("summary").asText(),
                    json.get("sentiment").asText(),
                    json.get("category").asText(),
                    json.get("priority").asText()
            );
            validateAnalyzeResponse(result);
            return result;
        } catch (InvalidAiResponseException exception) {
            throw exception;
        } catch (JsonProcessingException exception) {
            throw new InvalidAiResponseException("Gemini returned invalid JSON.", exception);
        }
    }

    private void validateAnalyzeResponse(AnalyzeContent response) {
        if (response.summary() == null || response.summary().isBlank()
                || !SENTIMENTS.contains(response.sentiment())
                || !CATEGORIES.contains(response.category())
                || !PRIORITIES.contains(response.priority())) {
            throw new InvalidAiResponseException("Gemini returned invalid analyze field values.");
        }
    }

    static HttpOptions buildHttpOptions(
            int requestTimeoutSeconds,
            int maxAttempts,
            long retryBackoffMs
    ) {
        HttpRetryOptions retryOptions = HttpRetryOptions.builder()
                .attempts(maxAttempts)
                .httpStatusCodes(408, 429, 500, 502, 503, 504)
                .initialDelay(retryBackoffMs / 1_000.0)
                .maxDelay((retryBackoffMs * 2) / 1_000.0)
                .expBase(2.0)
                .jitter(0.0)
                .build();

        return HttpOptions.builder()
                .timeout(Math.multiplyExact(requestTimeoutSeconds, 1_000))
                .retryOptions(retryOptions)
                .build();
    }

    private void validateConfiguration(
            int requestTimeoutSeconds,
            int maxAttempts,
            long retryBackoffMs
    ) {
        if (requestTimeoutSeconds <= 0 || maxAttempts <= 0 || retryBackoffMs < 0) {
            throw new IllegalStateException("AI timeout and retry configuration is invalid.");
        }
    }

    private static String validateModel(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalStateException("Gemini model configuration is required.");
        }
        return model.trim();
    }

    private RuntimeException mapProviderException(RuntimeException exception) {
        if (exception instanceof AiProviderException aiProviderException) {
            return aiProviderException;
        }

        if (isTimeout(exception)) {
            return new AiTimeoutException("AI provider request timed out.", exception);
        }

        if (exception instanceof ApiException apiException
                && (apiException.code() == 408 || apiException.code() == 504)) {
            return new AiTimeoutException("AI provider request timed out.", exception);
        }

        if (exception instanceof ApiException || exception instanceof GenAiIOException) {
            return new AiProviderException("AI provider request failed.", exception);
        }

        return new AiProviderException("AI provider request failed.", exception);
    }

    private boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private TokenUsage getTokenUsage(GenerateContentResponse response) {
        Long inputTokens = response.usageMetadata()
                .flatMap(GenerateContentResponseUsageMetadata::promptTokenCount)
                .map(Integer::longValue)
                .orElse(null);
        Long outputTokens = response.usageMetadata()
                .flatMap(GenerateContentResponseUsageMetadata::candidatesTokenCount)
                .map(Integer::longValue)
                .orElse(null);
        return new TokenUsage(inputTokens, outputTokens);
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    @Override
    public String getModelName() {
        return model;
    }

    @FunctionalInterface
    interface ContentGenerator {

        GenerateContentResponse generate(
                String model,
                String content,
                GenerateContentConfig config
        );
    }

    private record AnalyzeContent(
            String summary,
            String sentiment,
            String category,
            String priority
    ) {
    }

    private record TokenUsage(Long inputTokens, Long outputTokens) {
    }

}
