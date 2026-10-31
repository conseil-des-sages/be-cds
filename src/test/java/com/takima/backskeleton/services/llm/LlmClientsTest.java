package com.takima.backskeleton.services.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlmClientsTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicReference<JsonNode> receivedBody = new AtomicReference<>();
    private final AtomicReference<String> receivedAuth = new AtomicReference<>();
    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private void respond(String path, int status, String body) {
        server.createContext(path, exchange -> {
            receivedBody.set(mapper.readTree(exchange.getRequestBody()));
            receivedAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }

    private LlmProperties.Provider provider() {
        LlmProperties.Provider provider = new LlmProperties.Provider();
        provider.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        provider.setApiKey("cle-de-test");
        return provider;
    }

    private LlmRequest request(boolean jsonMode) {
        return new LlmRequest("modele-test", "système", "question", 0.4f, 300, jsonMode);
    }

    @Test
    void openAiFormatStreamsDeltasAndReadsUsage() {
        respond("/chat/completions", 200, """
                data: {"choices":[{"delta":{"role":"assistant"}}]}

                data: {"choices":[{"delta":{"content":"Bon"}}]}

                data: {"choices":[{"delta":{"content":"jour"}}],"usage":{"completion_tokens":7}}

                data: [DONE]

                """);
        List<String> deltas = new ArrayList<>();

        LlmResult result = new OpenAiCompatibleClient(mapper).stream(provider(), request(true), 5, deltas::add);

        assertThat(deltas).containsExactly("Bon", "jour");
        assertThat(result.text()).isEqualTo("Bonjour");
        assertThat(result.tokenCount()).isEqualTo(7);
        assertThat(receivedAuth.get()).isEqualTo("Bearer cle-de-test");
        assertThat(receivedBody.get().path("stream").asBoolean()).isTrue();
        assertThat(receivedBody.get().path("max_tokens").asInt()).isEqualTo(300);
        assertThat(receivedBody.get().path("response_format").path("type").asText()).isEqualTo("json_object");
        assertThat(receivedBody.get().path("messages").path(0).path("role").asText()).isEqualTo("system");
    }

    @Test
    void ollamaFormatStreamsNdjson() {
        respond("/api/chat", 200, """
                {"message":{"role":"assistant","content":"Bon"},"done":false}
                {"message":{"role":"assistant","content":"soir"},"done":false}
                {"message":{"role":"assistant","content":""},"done":true,"eval_count":9}
                """);
        List<String> deltas = new ArrayList<>();

        LlmResult result = new OllamaClient(mapper).stream(provider(), request(true), 5, deltas::add);

        assertThat(result.text()).isEqualTo("Bonsoir");
        assertThat(result.tokenCount()).isEqualTo(9);
        assertThat(receivedBody.get().path("options").path("num_predict").asInt()).isEqualTo(300);
        assertThat(receivedBody.get().path("format").asText()).isEqualTo("json");
    }

    @Test
    void rateLimitIsRetryableButBadKeyIsNot() {
        respond("/limit/chat/completions", 429, "{}");
        respond("/key/chat/completions", 401, "{}");
        LlmProperties.Provider limited = provider();
        limited.setBaseUrl(limited.getBaseUrl() + "/limit");
        LlmProperties.Provider badKey = provider();
        badKey.setBaseUrl(badKey.getBaseUrl() + "/key");
        OpenAiCompatibleClient client = new OpenAiCompatibleClient(mapper);

        assertThatThrownBy(() -> client.stream(limited, request(false), 5, delta -> { }))
                .isInstanceOfSatisfying(LlmException.class, e -> assertThat(e.isRetryable()).isTrue());
        assertThatThrownBy(() -> client.stream(badKey, request(false), 5, delta -> { }))
                .isInstanceOfSatisfying(LlmException.class, e -> assertThat(e.isRetryable()).isFalse());
    }

    @Test
    void emptyAnswerIsAFailure() {
        respond("/chat/completions", 200, "data: [DONE]\n\n");

        assertThatThrownBy(() -> new OpenAiCompatibleClient(mapper).stream(provider(), request(false), 5, delta -> { }))
                .isInstanceOf(LlmException.class);
    }
}
