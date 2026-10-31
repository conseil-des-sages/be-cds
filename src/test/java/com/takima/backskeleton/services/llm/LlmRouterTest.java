package com.takima.backskeleton.services.llm;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlmRouterTest {
    /** Un client dont chaque appel consomme le prochain comportement prévu. */
    private static final class ScriptedClient extends LlmStreamClient {
        private final List<RuntimeException> failures = new ArrayList<>();
        private final List<String> calledModels = new ArrayList<>();

        ScriptedClient() {
            super(null);
        }

        @Override
        LlmProperties.ProviderType type() {
            return LlmProperties.ProviderType.OPENAI_COMPATIBLE;
        }

        @Override
        String path() {
            return "";
        }

        @Override
        JsonNode body(LlmRequest request) {
            return null;
        }

        @Override
        boolean onLine(String line, Accumulator accumulator) {
            return false;
        }

        @Override
        LlmResult stream(LlmProperties.Provider provider, LlmRequest request, int timeout, Consumer<String> onDelta) {
            calledModels.add(request.model());
            if (!failures.isEmpty()) {
                throw failures.remove(0);
            }
            onDelta.accept("ok");
            return new LlmResult("ok", request.model(), request.maxTokens());
        }
    }

    private final ScriptedClient client = new ScriptedClient();
    private int retries;
    private final LlmListener listener = new LlmListener() {
        @Override
        public void onDelta(String delta) {
        }

        @Override
        public void onRetry() {
            retries++;
        }
    };

    private LlmProperties.Provider provider(String key, int extraTokens) {
        LlmProperties.Provider provider = new LlmProperties.Provider();
        provider.setType(LlmProperties.ProviderType.OPENAI_COMPATIBLE);
        provider.setApiKey(key);
        provider.setExtraMaxTokens(extraTokens);
        return provider;
    }

    private LlmProperties.Model model(String id, String provider) {
        LlmProperties.Model model = new LlmProperties.Model();
        model.setId(id);
        model.setProvider(provider);
        return model;
    }

    private LlmRouter router(String mistralKey, String geminiKey) {
        LlmProperties properties = new LlmProperties();
        properties.setDefaultModel("mistral-small");
        properties.getProviders().put("mistral", provider(mistralKey, 0));
        properties.getProviders().put("gemini", provider(geminiKey, 1000));
        properties.getModels().add(model("mistral-small", "mistral"));
        properties.getModels().add(model("gemini-flash", "gemini"));
        return new LlmRouter(properties, List.of(client), 0);
    }

    private LlmRequest request(String model) {
        return new LlmRequest(model, "s", "p", 0.5f, 300, false);
    }

    @Test
    void usesTheRequestedModelWithItsExtraTokens() {
        LlmResult result = router("k", "k").generate(request("gemini-flash"), listener);

        assertThat(result.model()).isEqualTo("gemini-flash");
        assertThat(result.tokenCount()).isEqualTo(1300);
    }

    @Test
    void fallsBackWhenTheProviderHasNoKey() {
        LlmResult result = router("k", "").generate(request("gemini-flash"), listener);

        assertThat(result.model()).isEqualTo("mistral-small");
        assertThat(client.calledModels).containsExactly("mistral-small");
    }

    @Test
    void retriesThenSwitchesProviderWhenAModelKeepsFailing() {
        LlmException rateLimited = new LlmException("429", true);
        client.failures.addAll(List.of(rateLimited, rateLimited));

        LlmResult result = router("k", "k").generate(request("mistral-small"), listener);

        assertThat(client.calledModels)
                .containsExactly("mistral-small", "mistral-small", "gemini-flash");
        assertThat(result.model()).isEqualTo("gemini-flash");
        assertThat(retries).isEqualTo(2);
    }

    @Test
    void doesNotRetryARefusedKey() {
        client.failures.add(new LlmException("401", false));

        router("k", "k").generate(request("mistral-small"), listener);

        assertThat(client.calledModels).containsExactly("mistral-small", "gemini-flash");
    }

    @Test
    void failsClearlyWithoutAnyKey() {
        assertThatThrownBy(() -> router("", "").generate(request("mistral-small"), listener))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("clé");
    }
}
