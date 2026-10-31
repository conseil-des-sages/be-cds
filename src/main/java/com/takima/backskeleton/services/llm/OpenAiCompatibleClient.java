package com.takima.backskeleton.services.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

/** Format /chat/completions, utilisé par Mistral et par l'API de compatibilité de Gemini. */
@Component
class OpenAiCompatibleClient extends LlmStreamClient {
    private static final String DATA_PREFIX = "data:";

    OpenAiCompatibleClient(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    LlmProperties.ProviderType type() {
        return LlmProperties.ProviderType.OPENAI_COMPATIBLE;
    }

    @Override
    String path() {
        return "/chat/completions";
    }

    @Override
    JsonNode body(LlmRequest request) {
        ObjectNode body = mapper.createObjectNode()
                .put("model", request.model())
                .put("stream", true)
                .put("temperature", request.temperature())
                .put("max_tokens", request.maxTokens());
        var messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", request.system());
        messages.addObject().put("role", "user").put("content", request.prompt());
        if (request.jsonMode()) {
            body.putObject("response_format").put("type", "json_object");
        }
        return body;
    }

    @Override
    boolean onLine(String line, Accumulator accumulator) throws JsonProcessingException {
        if (!line.startsWith(DATA_PREFIX)) {
            return true;
        }
        String data = line.substring(DATA_PREFIX.length()).trim();
        if ("[DONE]".equals(data)) {
            return false;
        }
        JsonNode chunk = mapper.readTree(data);
        JsonNode content = chunk.path("choices").path(0).path("delta").path("content");
        if (content.isTextual()) {
            accumulator.append(content.asText());
        }
        JsonNode tokens = chunk.path("usage").path("completion_tokens");
        if (tokens.isInt()) {
            accumulator.tokenCount(tokens.asInt());
        }
        return true;
    }
}
