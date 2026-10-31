package com.takima.backskeleton.services.llm;

public record LlmRequest(
        String model,
        String system,
        String prompt,
        float temperature,
        int maxTokens,
        boolean jsonMode
) {
    public LlmRequest forTarget(String targetModel, int extraMaxTokens) {
        return new LlmRequest(targetModel, system, prompt, temperature, maxTokens + extraMaxTokens, jsonMode);
    }
}
