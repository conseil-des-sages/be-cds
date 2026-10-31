package com.takima.backskeleton.services.debate;

import com.takima.backskeleton.DTO.DebateDtos;
import com.takima.backskeleton.services.debate.DebateSnapshot.Speaker;
import com.takima.backskeleton.services.llm.LlmException;
import com.takima.backskeleton.services.llm.LlmListener;
import com.takima.backskeleton.services.llm.LlmResult;
import com.takima.backskeleton.services.llm.LlmRouter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Le Greffier, en dehors du déroulé du débat : il observe les interventions sur son propre fil et fait
 * une remarque à peu près toutes les demi-tablées. Les sages ne l'attendent jamais et ne le lisent pas.
 */
@Service
@RequiredArgsConstructor
public class ScribeWatcher {
    private static final Logger logger = LoggerFactory.getLogger(ScribeWatcher.class);
    private static final int MIN_TURNS_BETWEEN_NOTES = 2;
    private static final LlmListener NO_STREAMING = new LlmListener() {
        @Override
        public void onDelta(String delta) {
            // La remarque n'est affichée qu'une fois complète.
        }

        @Override
        public void onRetry() {
            // Rien n'a été affiché.
        }
    };

    private final DebateStore store;
    private final DebatePrompts prompts;
    private final LlmRouter llm;
    private final DebateEventHub hub;
    private final TaskExecutor scribeExecutor;

    private final Set<Integer> writing = ConcurrentHashMap.newKeySet();

    /** Appelé après chaque intervention. Rend la main tout de suite ; une remarque déjà en cours suffit. */
    public void onTurnSpoken(Integer sessionId) {
        if (!writing.add(sessionId)) {
            return;
        }
        try {
            scribeExecutor.execute(() -> {
                try {
                    writeNoteIfDue(sessionId);
                } catch (RuntimeException exception) {
                    // Une remarque ratée ne doit jamais gêner le débat.
                    logger.warn("Remarque du Greffier ignorée pour le débat {} : {}", sessionId, exception.getMessage());
                } finally {
                    writing.remove(sessionId);
                }
            });
        } catch (TaskRejectedException exception) {
            writing.remove(sessionId);
        }
    }

    private void writeNoteIfDue(Integer sessionId) throws LlmException {
        DebateSnapshot debate = store.load(sessionId);
        Set<Integer> sageIds = debate.sages().stream().map(Speaker::id).collect(Collectors.toSet());
        long rounds = debate.steps().stream().filter(step -> step.order() != DebateScript.SYNTHESIS_ORDER).count();
        int spoken = (int) debate.steps().stream()
                .flatMap(step -> step.turns().stream())
                .filter(turn -> sageIds.contains(turn.participantId()))
                .count();
        int interval = Math.max(MIN_TURNS_BETWEEN_NOTES, (debate.sages().size() + 1) / 2);
        int due = spoken - spoken % interval;
        List<DebateDtos.NoteView> notes = store.loadNotes(sessionId);
        boolean alreadyWritten = notes.stream().anyMatch(note -> note.afterTurns() >= due);
        // Le dernier mot revient à la synthèse.
        if (due == 0 || spoken >= rounds * debate.sages().size() || alreadyWritten) {
            return;
        }
        LlmResult result = llm.generate(prompts.forNote(debate, notes), NO_STREAMING);
        hub.publish(sessionId, "note", store.saveNote(sessionId, due, result.text(), result.model()));
    }
}
