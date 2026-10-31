package com.takima.backskeleton.services.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Base commune des clients : envoie la requête, lit la réponse ligne par ligne et traduit
 * les erreurs réseau et HTTP en {@link LlmException}. Chaque format ne décrit que son corps et ses lignes.
 */
abstract class LlmStreamClient {
    /** Un fournisseur surchargé peut attendre longtemps avant de refuser : le débat bascule plus tôt. */
    private static final int FIRST_RESPONSE_TIMEOUT_SECONDS = 45;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    protected final ObjectMapper mapper;

    protected LlmStreamClient(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    abstract LlmProperties.ProviderType type();

    abstract String path();

    abstract JsonNode body(LlmRequest request);

    /** Traite une ligne du flux et renvoie false quand le fournisseur a signalé la fin. */
    abstract boolean onLine(String line, Accumulator accumulator) throws JsonProcessingException;

    LlmResult stream(LlmProperties.Provider provider, LlmRequest request, int timeoutSeconds, Consumer<String> onDelta) {
        Accumulator accumulator = new Accumulator(onDelta);
        HttpResponse<Stream<String>> response = send(provider, request, timeoutSeconds);

        // Le délai de HttpRequest ne couvre que les en-têtes : ce minuteur coupe aussi un flux qui s'arrête en route.
        try (Stream<String> lines = response.body()) {
            CompletableFuture<Void> watchdog = CompletableFuture.runAsync(lines::close,
                    CompletableFuture.delayedExecutor(timeoutSeconds, TimeUnit.SECONDS));
            try {
                var iterator = lines.iterator();
                while (iterator.hasNext() && onLine(iterator.next(), accumulator)) {
                    // la ligne a été traitée par onLine
                }
            } catch (JsonProcessingException exception) {
                throw new LlmException("Réponse illisible du fournisseur.", true, exception);
            } catch (UncheckedIOException | IllegalStateException exception) {
                throw new LlmException("Le flux du fournisseur s'est interrompu.", true, exception);
            } finally {
                watchdog.cancel(false);
            }
        }

        String text = accumulator.text().trim();
        if (text.isEmpty()) {
            throw new LlmException("Le fournisseur a renvoyé une réponse vide.", true);
        }
        return new LlmResult(text, request.model(), accumulator.tokenCount());
    }

    private HttpResponse<Stream<String>> send(LlmProperties.Provider provider, LlmRequest request, int timeoutSeconds) {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(provider.getBaseUrl() + path()))
                .timeout(Duration.ofSeconds(Math.min(timeoutSeconds, FIRST_RESPONSE_TIMEOUT_SECONDS)))
                .header("Authorization", "Bearer " + provider.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body(request).toString()))
                .build();
        HttpResponse<Stream<String>> response;
        try {
            response = http.send(httpRequest, HttpResponse.BodyHandlers.ofLines());
        } catch (IOException exception) {
            throw new LlmException("Fournisseur injoignable : " + exception.getMessage(), true, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LlmException("Requête interrompue.", false, exception);
        }
        int status = response.statusCode();
        if (status >= 300) {
            response.body().close();
            throw new LlmException("Le fournisseur a répondu " + status + ".", status == 429 || status >= 500);
        }
        return response;
    }

    /** Assemble le texte reçu et le transmet au fil de l'eau. */
    static final class Accumulator {
        private final Consumer<String> onDelta;
        private final StringBuilder text = new StringBuilder();
        private Integer tokenCount;

        Accumulator(Consumer<String> onDelta) {
            this.onDelta = onDelta;
        }

        void append(String delta) {
            if (delta != null && !delta.isEmpty()) {
                text.append(delta);
                onDelta.accept(delta);
            }
        }

        void tokenCount(Integer count) {
            tokenCount = count;
        }

        String text() {
            return text.toString();
        }

        Integer tokenCount() {
            return tokenCount;
        }
    }
}
