package com.takima.backskeleton.services.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

/** API native d'Ollama (/api/chat) : un objet JSON par ligne. */
@Component
class OllamaClient extends LlmStreamClient {
    OllamaClient(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    LlmProperties.ProviderType type() {
        return LlmProperties.ProviderType.OLLAMA;
    }

    @Override
    String path() {
        return "/api/chat";
    }

    @Override
    JsonNode body(LlmRequest request) {
        ObjectNode body = mapper.createObjectNode()
                .put("model", request.model())
                .put("stream", true);
        var messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", request.system());
        messages.addObject().put("role", "user").put("content", request.prompt());
        body.putObject("options")
                .put("temperature", request.temperature())
                .put("num_predict", request.maxTokens());
        if (request.jsonMode()) {
            body.put("format", "json");
        }
        return body;
    }

    @Override
    boolean onLine(String line, Accumulator accumulator) throws JsonProcessingException {
        if (line.isBlank()) {
            return true;
        }
        JsonNode chunk = mapper.readTree(line);
        if (chunk.hasNonNull("error")) {
            throw new LlmException("Ollama : " + chunk.path("error").asText(), true);
        }
        JsonNode content = chunk.path("message").path("content");
        if (content.isTextual()) {
            accumulator.append(content.asText());
        }
        if (chunk.path("done").asBoolean(false)) {
            JsonNode tokens = chunk.path("eval_count");
            if (tokens.isInt()) {
                accumulator.tokenCount(tokens.asInt());
            }
            return false;
        }
        return true;
    }
}
