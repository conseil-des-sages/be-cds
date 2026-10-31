package com.takima.backskeleton.services.debate;

import com.takima.backskeleton.DTO.DebateDtos;
import com.takima.backskeleton.models.SessionStatus;
import com.takima.backskeleton.models.StepStatus;
import com.takima.backskeleton.services.debate.DebateSnapshot.Speaker;
import com.takima.backskeleton.services.debate.DebateSnapshot.StepState;
import com.takima.backskeleton.services.debate.DebateSnapshot.Turn;
import com.takima.backskeleton.services.llm.LlmException;
import com.takima.backskeleton.services.llm.LlmResult;
import com.takima.backskeleton.services.llm.LlmRouter;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScribeWatcherTest {
    private static final Integer SESSION = 1;
    private static final Speaker A = new Speaker(2, "Le Prudent", "Prudent", "p", "m", 0.5f, 600);
    private static final Speaker B = new Speaker(3, "Le Financier", "Financier", "p", "m", 0.5f, 600);
    private static final Speaker SCRIBE = new Speaker(7, "Le Greffier", "Greffier", "p", "m", 0.2f, 2000);

    private final DebateStore store = mock(DebateStore.class);
    private final LlmRouter llm = mock(LlmRouter.class);
    private final DebateEventHub hub = mock(DebateEventHub.class);
    private final ScribeWatcher watcher =
            new ScribeWatcher(store, new DebatePrompts(), llm, hub, new SyncTaskExecutor());

    /** Deux sages et deux tours : une remarque est due toutes les 2 interventions. */
    private void givenSpokenTurns(int spoken) {
        List<Turn> turns = new ArrayList<>();
        for (int i = 0; i < spoken; i++) {
            turns.add(new Turn(i, A.id(), A.name(), "Avis " + i));
        }
        List<StepState> steps = List.of(
                new StepState(1, 1, "Tour 1", "c", StepStatus.IN_PROGRESS, turns),
                new StepState(2, 2, "Tour 2", "c", StepStatus.PENDING, List.of()));
        when(store.load(SESSION)).thenReturn(new DebateSnapshot(SESSION, "Sam", "Question ?", null,
                SessionStatus.IN_PROGRESS, List.of(A, B), SCRIBE, steps));
    }

    private DebateDtos.NoteView note(int afterTurns) {
        return new DebateDtos.NoteView(afterTurns, afterTurns, "Remarque", LocalDateTime.now());
    }

    @Test
    void writesARemarkWhenOneIsDue() {
        givenSpokenTurns(2);
        DebateDtos.NoteView saved = note(2);
        when(llm.generate(any(), any())).thenReturn(new LlmResult("Ils s'opposent.", "m", 5));
        when(store.saveNote(SESSION, 2, "Ils s'opposent.", "m")).thenReturn(saved);

        watcher.onTurnSpoken(SESSION);

        verify(hub).publish(SESSION, "note", saved);
    }

    @Test
    void staysSilentBetweenRemarks() {
        givenSpokenTurns(1);

        watcher.onTurnSpoken(SESSION);

        verify(llm, never()).generate(any(), any());
    }

    @Test
    void doesNotRepeatARemarkAfterAResume() {
        givenSpokenTurns(2);
        when(store.loadNotes(SESSION)).thenReturn(List.of(note(2)));

        watcher.onTurnSpoken(SESSION);

        verify(llm, never()).generate(any(), any());
    }

    @Test
    void leavesTheLastWordToTheSynthesis() {
        givenSpokenTurns(4);

        watcher.onTurnSpoken(SESSION);

        verify(llm, never()).generate(any(), any());
    }

    @Test
    void aFailedRemarkIsIgnoredAndTheNextOneCanStillBeWritten() {
        givenSpokenTurns(2);
        when(llm.generate(any(), any())).thenThrow(new LlmException("503", true));

        watcher.onTurnSpoken(SESSION);
        watcher.onTurnSpoken(SESSION);

        verify(llm, org.mockito.Mockito.times(2)).generate(any(), any());
        verify(store, never()).saveNote(eq(SESSION), anyInt(), any(), any());
    }
}
