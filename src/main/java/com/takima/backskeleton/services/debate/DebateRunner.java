package com.takima.backskeleton.services.debate;

import com.fasterxml.jackson.databind.JsonNode;
import com.takima.backskeleton.DTO.DebateDtos;
import com.takima.backskeleton.DTO.LlmModelDto;
import com.takima.backskeleton.models.SessionStatus;
import com.takima.backskeleton.models.StepStatus;
import com.takima.backskeleton.services.debate.DebateSnapshot.Speaker;
import com.takima.backskeleton.services.debate.DebateSnapshot.StepState;
import com.takima.backskeleton.services.debate.DebateSnapshot.Turn;
import com.takima.backskeleton.services.llm.LlmException;
import com.takima.backskeleton.services.llm.LlmListener;
import com.takima.backskeleton.services.llm.LlmRequest;
import com.takima.backskeleton.services.llm.LlmResult;
import com.takima.backskeleton.services.llm.LlmRouter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fait dérouler un débat. Chaque message est enregistré dès qu'il est complet : après une panne ou un
 * redémarrage, le moteur repart simplement du premier sage qui n'a pas encore parlé.
 */
@Service
@RequiredArgsConstructor
public class DebateRunner {
    private static final Logger logger = LoggerFactory.getLogger(DebateRunner.class);
    private static final int SYNTHESIS_ATTEMPTS = 2;

    private final DebateStore store;
    private final DebatePrompts prompts;
    private final LlmRouter llm;
    private final SynthesisParser synthesisParser;
    private final DebateEventHub hub;
    private final ScribeWatcher scribeWatcher;
    private final TaskExecutor debateExecutor;

    private final Set<Integer> running = ConcurrentHashMap.newKeySet();

    /** Lance le débat s'il reste quelque chose à faire. Sans effet s'il tourne déjà. */
    public void ensureRunning(Integer sessionId) {
        if (running.add(sessionId)) {
            debateExecutor.execute(() -> run(sessionId));
        }
    }

    void run(Integer sessionId) {
        try {
            DebateSnapshot debate = store.load(sessionId);
            if (debate.status() != SessionStatus.IN_PROGRESS) {
                hub.publish(sessionId, "status", Map.of("status", debate.status()));
                return;
            }
            String theme = SynthesisParser.OTHER_THEME;
            for (StepState step : debate.steps()) {
                if (step.status() == StepStatus.DONE) {
                    continue;
                }
                startStep(sessionId, step);
                if (step.order() == DebateScript.SYNTHESIS_ORDER) {
                    theme = writeSynthesis(sessionId, step);
                } else {
                    runRound(sessionId, step.id());
                }
                finishStep(sessionId, step);
            }
            store.finish(sessionId, theme);
            hub.publish(sessionId, "status", Map.of("status", SessionStatus.FINISHED));
        } catch (LlmException exception) {
            logger.warn("Débat {} interrompu : {}", sessionId, exception.getMessage());
            boolean noProvider = llm.catalog().stream().noneMatch(LlmModelDto::available);
            hub.publish(sessionId, "failure", Map.of("detail", noProvider
                    ? "Aucune clé d'API n'est configurée sur le serveur."
                    : "Le conseil n'a pas pu continuer."));
        } catch (RuntimeException exception) {
            logger.error("Erreur inattendue dans le débat {}", sessionId, exception);
            hub.publish(sessionId, "failure", Map.of("detail", "Une erreur est survenue."));
        } finally {
            hub.endTurn(sessionId);
            running.remove(sessionId);
            hub.close(sessionId);
        }
    }

    private void runRound(Integer sessionId, Integer stepId) {
        int seats = store.load(sessionId).sages().size();
        for (int seat = 0; seat < seats; seat++) {
            // Relu à chaque tour : le sage suivant tient compte de ce que la personne vient d'ajouter.
            DebateSnapshot debate = store.load(sessionId);
            StepState step = stepOf(debate, stepId);
            Speaker sage = debate.sages().get(seat);
            if (!step.hasSpoken(sage.id())) {
                speak(debate, step, sage);
            }
        }
    }

    private void speak(DebateSnapshot debate, StepState step, Speaker sage) {
        Integer sessionId = debate.sessionId();
        hub.beginTurn(sessionId, sage.id(), step.id());
        LlmResult result = llm.generate(prompts.forSage(debate, step, sage), streamTo(sessionId));
        DebateDtos.MessageView message = store.append(step.id(), sage.id(), result.text(), result.model(),
                result.tokenCount(), findRepliedMessage(debate, sage, result.text()));
        hub.endTurn(sessionId);
        hub.publish(sessionId, "message", message);
        scribeWatcher.onTurnSpoken(sessionId);
    }

    /** Le dernier message du sage que celui-ci interpelle, pour afficher « répond au Financier ». */
    private Integer findRepliedMessage(DebateSnapshot debate, Speaker author, String content) {
        for (Speaker other : debate.sages()) {
            if (!other.id().equals(author.id()) && mentions(content, other)) {
                Optional<Integer> message = lastMessageOf(debate, other.id());
                if (message.isPresent()) {
                    return message.get();
                }
            }
        }
        return null;
    }

    private boolean mentions(String content, Speaker sage) {
        return content.contains(sage.name()) || (sage.shortName() != null && content.contains(sage.shortName()));
    }

    private Optional<Integer> lastMessageOf(DebateSnapshot debate, Integer participantId) {
        List<StepState> steps = debate.steps();
        for (int i = steps.size() - 1; i >= 0; i--) {
            List<Turn> turns = steps.get(i).turns();
            for (int j = turns.size() - 1; j >= 0; j--) {
                if (turns.get(j).participantId().equals(participantId)) {
                    return Optional.of(turns.get(j).messageId());
                }
            }
        }
        return Optional.empty();
    }

    private String writeSynthesis(Integer sessionId, StepState step) {
        DebateSnapshot debate = store.load(sessionId);
        LlmRequest request = prompts.forScribe(debate);
        String raw = null;
        for (int attempt = 0; attempt < SYNTHESIS_ATTEMPTS; attempt++) {
            LlmResult result = llm.generate(request, silent());
            raw = result.text();
            Optional<JsonNode> synthesis = synthesisParser.parse(raw);
            if (synthesis.isPresent()) {
                store.append(step.id(), debate.scribe().id(), synthesis.get().toString(),
                        result.model(), result.tokenCount(), null);
                hub.publish(sessionId, "synthesis", synthesis.get());
                return synthesisParser.theme(synthesis.get());
            }
            logger.warn("Synthèse illisible pour le débat {} (tentative {})", sessionId, attempt + 1);
        }
        throw new LlmException("Le Greffier n'a pas rendu de synthèse lisible : " + abbreviate(raw), false);
    }

    private void startStep(Integer sessionId, StepState step) {
        store.setStepStatus(step.id(), StepStatus.IN_PROGRESS);
        hub.publish(sessionId, "step", Map.of("stepId", step.id(), "status", StepStatus.IN_PROGRESS));
    }

    private void finishStep(Integer sessionId, StepState step) {
        store.setStepStatus(step.id(), StepStatus.DONE);
        hub.publish(sessionId, "step", Map.of("stepId", step.id(), "status", StepStatus.DONE));
    }

    private StepState stepOf(DebateSnapshot debate, Integer stepId) {
        return debate.steps().stream().filter(step -> step.id().equals(stepId)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Étape " + stepId + " introuvable."));
    }

    private LlmListener streamTo(Integer sessionId) {
        return new LlmListener() {
            @Override
            public void onDelta(String delta) {
                hub.delta(sessionId, delta);
            }

            @Override
            public void onRetry() {
                hub.resetTurn(sessionId);
            }
        };
    }

    private LlmListener silent() {
        return new LlmListener() {
            @Override
            public void onDelta(String delta) {
                // La synthèse n'est affichée qu'une fois complète.
            }

            @Override
            public void onRetry() {
                // Rien n'a été affiché.
            }
        };
    }

    private String abbreviate(String text) {
        return text == null || text.length() <= 200 ? text : text.substring(0, 200);
    }
}
