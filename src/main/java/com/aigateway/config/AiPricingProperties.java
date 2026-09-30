package com.aigateway.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai.pricing")
public class AiPricingProperties {

    private List<ModelPricing> models = new ArrayList<>();

    public List<ModelPricing> getModels() {
        return models;
    }

    public void setModels(List<ModelPricing> models) {
        this.models = models;
    }

    public Optional<ModelPricing> find(String provider, String model) {
        if (provider == null || model == null) {
            return Optional.empty();
        }

        return models.stream()
                .filter(pricing -> provider.equals(pricing.getProvider()))
                .filter(pricing -> model.equals(pricing.getModel()))
                .filter(ModelPricing::isComplete)
                .findFirst();
    }

    public static class ModelPricing {

        private String provider;
        private String model;
        private BigDecimal inputPerMillion;
        private BigDecimal outputPerMillion;

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

        public BigDecimal getInputPerMillion() {
            return inputPerMillion;
        }

        public void setInputPerMillion(BigDecimal inputPerMillion) {
            this.inputPerMillion = inputPerMillion;
        }

        public BigDecimal getOutputPerMillion() {
            return outputPerMillion;
        }

        public void setOutputPerMillion(BigDecimal outputPerMillion) {
            this.outputPerMillion = outputPerMillion;
        }

        private boolean isComplete() {
            return inputPerMillion != null
                    && outputPerMillion != null
                    && inputPerMillion.signum() >= 0
                    && outputPerMillion.signum() >= 0;
        }
    }
}
