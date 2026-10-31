package com.takima.backskeleton.services.debate;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Relaie les événements d'un débat à ses navigateurs connectés. Le tour en cours est gardé en mémoire :
 * un navigateur qui se reconnecte retrouve le texte déjà écrit par le sage.
 */
@Component
public class DebateEventHub {
    private static final long EMITTER_TIMEOUT_MS = 10 * 60 * 1000L;

    private final Map<Integer, Topic> topics = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Integer sessionId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        Topic topic = topics.computeIfAbsent(sessionId, id -> new Topic());
        emitter.onCompletion(() -> topic.remove(emitter));
        emitter.onTimeout(emitter::complete);
        emitter.onError(error -> topic.remove(emitter));
        topic.add(emitter);
        return emitter;
    }

    /** Sans navigateur connecté, l'événement est simplement perdu : tout reste relisible en base. */
    public void publish(Integer sessionId, String event, Object payload) {
        Topic topic = topics.get(sessionId);
        if (topic != null) {
            topic.send(event, payload);
        }
    }

    public void beginTurn(Integer sessionId, Integer sageId, Integer stepId) {
        topic(sessionId).beginTurn(sageId, stepId);
    }

    public void delta(Integer sessionId, String text) {
        topic(sessionId).delta(text);
    }

    public void resetTurn(Integer sessionId) {
        topic(sessionId).resetTurn();
    }

    public void endTurn(Integer sessionId) {
        topic(sessionId).endTurn();
    }

    /** Fin du débat, ou panne : les navigateurs ferment leur flux. */
    public void close(Integer sessionId) {
        Topic topic = topics.remove(sessionId);
        if (topic != null) {
            topic.complete();
        }
    }

    private Topic topic(Integer sessionId) {
        return topics.computeIfAbsent(sessionId, id -> new Topic());
    }

    private static final class Topic {
        private final List<SseEmitter> emitters = new ArrayList<>();
        private final StringBuilder currentText = new StringBuilder();
        private Integer currentSage;
        private Integer currentStep;

        synchronized void add(SseEmitter emitter) {
            emitters.add(emitter);
            if (currentSage != null) {
                sendTo(emitter, "turn", turnPayload());
                if (!currentText.isEmpty()) {
                    sendTo(emitter, "delta", Map.of("text", currentText.toString()));
                }
            }
        }

        synchronized void remove(SseEmitter emitter) {
            emitters.remove(emitter);
        }

        synchronized void beginTurn(Integer sageId, Integer stepId) {
            currentSage = sageId;
            currentStep = stepId;
            currentText.setLength(0);
            send("turn", turnPayload());
        }

        synchronized void delta(String text) {
            currentText.append(text);
            send("delta", Map.of("text", text));
        }

        synchronized void resetTurn() {
            currentText.setLength(0);
            send("reset", Map.of());
        }

        synchronized void endTurn() {
            currentSage = null;
            currentStep = null;
            currentText.setLength(0);
        }

        synchronized void send(String event, Object payload) {
            for (SseEmitter emitter : List.copyOf(emitters)) {
                sendTo(emitter, event, payload);
            }
        }

        synchronized void complete() {
            List.copyOf(emitters).forEach(SseEmitter::complete);
            emitters.clear();
        }

        private Map<String, Integer> turnPayload() {
            return Map.of("sageId", currentSage, "stepId", currentStep);
        }

        private void sendTo(SseEmitter emitter, String event, Object payload) {
            try {
                emitter.send(SseEmitter.event().name(event).data(payload));
            } catch (IOException | IllegalStateException exception) {
                // Navigateur parti : le débat continue et ce flux est simplement retiré.
                emitters.remove(emitter);
            }
        }
    }
}
