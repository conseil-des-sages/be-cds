package com.takima.backskeleton.services.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties("app.llm")
public class LlmProperties {
    private int timeoutSeconds = 120;
    private String defaultModel;
    private Map<String, Provider> providers = new LinkedHashMap<>();
    private List<Model> models = new ArrayList<>();

    public enum ProviderType {
        OPENAI_COMPATIBLE,
        OLLAMA
    }

    @Data
    public static class Provider {
        private String label;
        private ProviderType type;
        private String baseUrl;
        private String apiKey;
        private int extraMaxTokens;

        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }
    }

    @Data
    public static class Model {
        private String id;
        private String provider;
        private String label;
    }
}
