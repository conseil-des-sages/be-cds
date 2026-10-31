package com.takima.backskeleton.services.llm;

import com.takima.backskeleton.DTO.LlmModelDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Choisit le fournisseur d'une requête. Si le modèle demandé est indisponible ou échoue,
 * le débat continue avec le modèle par défaut, puis avec un modèle de chaque autre fournisseur.
 */
@Service
public class LlmRouter {
    private static final Logger logger = LoggerFactory.getLogger(LlmRouter.class);
    private static final long[] RETRY_DELAYS_MS = {2_000};

    private final LlmProperties properties;
    private final Map<LlmProperties.ProviderType, LlmStreamClient> clients;
    private final long retryDelayFactor;

    @Autowired
    public LlmRouter(LlmProperties properties, List<LlmStreamClient> clients) {
        this(properties, clients, 1);
    }

    LlmRouter(LlmProperties properties, List<LlmStreamClient> clients, long retryDelayFactor) {
        this.properties = properties;
        this.clients = clients.stream().collect(Collectors.toMap(LlmStreamClient::type, Function.identity()));
        this.retryDelayFactor = retryDelayFactor;
    }

    public List<LlmModelDto> catalog() {
        return properties.getModels().stream()
                .map(model -> new LlmModelDto(model.getId(), model.getLabel(), providerLabel(model), isAvailable(model)))
                .toList();
    }

    public boolean isKnownModel(String modelId) {
        return properties.getModels().stream().anyMatch(model -> model.getId().equals(modelId));
    }

    public LlmResult generate(LlmRequest request, LlmListener listener) {
        List<LlmProperties.Model> candidates = candidates(request.model());
        if (candidates.isEmpty()) {
            throw new LlmException("Aucune clé d'API n'est configurée.", false);
        }
        LlmException lastFailure = null;
        boolean firstAttempt = true;
        for (LlmProperties.Model candidate : candidates) {
            for (int attempt = 0; attempt <= RETRY_DELAYS_MS.length; attempt++) {
                if (!firstAttempt) {
                    listener.onRetry();
                }
                firstAttempt = false;
                try {
                    return attempt(candidate, request, listener);
                } catch (LlmException exception) {
                    lastFailure = exception;
                    logger.warn("Échec du modèle {} : {}", candidate.getId(), exception.getMessage());
                    if (!exception.isRetryable() || attempt == RETRY_DELAYS_MS.length) {
                        break;
                    }
                    pause(RETRY_DELAYS_MS[attempt] * retryDelayFactor);
                }
            }
        }
        throw lastFailure;
    }

    private LlmResult attempt(LlmProperties.Model model, LlmRequest request, LlmListener listener) {
        LlmProperties.Provider provider = properties.getProviders().get(model.getProvider());
        LlmStreamClient client = clients.get(provider.getType());
        LlmRequest target = request.forTarget(model.getId(), provider.getExtraMaxTokens());
        return client.stream(provider, target, properties.getTimeoutSeconds(), listener::onDelta);
    }

    /** Le modèle demandé, le modèle par défaut, puis le premier modèle de chaque autre fournisseur utilisable. */
    private List<LlmProperties.Model> candidates(String requestedModel) {
        List<LlmProperties.Model> available = properties.getModels().stream().filter(this::isAvailable).toList();
        List<LlmProperties.Model> ordered = new ArrayList<>();
        available.stream().filter(model -> model.getId().equals(requestedModel)).findFirst().ifPresent(ordered::add);
        available.stream().filter(model -> model.getId().equals(properties.getDefaultModel()))
                .filter(model -> !ordered.contains(model)).findFirst().ifPresent(ordered::add);
        for (LlmProperties.Model model : available) {
            boolean providerTried = ordered.stream().anyMatch(other -> other.getProvider().equals(model.getProvider()));
            if (!providerTried) {
                ordered.add(model);
            }
        }
        return ordered;
    }

    private boolean isAvailable(LlmProperties.Model model) {
        LlmProperties.Provider provider = properties.getProviders().get(model.getProvider());
        return provider != null && provider.isConfigured();
    }

    private String providerLabel(LlmProperties.Model model) {
        LlmProperties.Provider provider = properties.getProviders().get(model.getProvider());
        return provider == null ? model.getProvider() : provider.getLabel();
    }

    private void pause(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LlmException("Requête interrompue.", false, exception);
        }
    }
}
