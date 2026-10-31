package com.takima.backskeleton.services.debate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takima.backskeleton.DTO.DebateDtos;
import com.takima.backskeleton.models.SessionStatus;
import com.takima.backskeleton.models.StepStatus;
import com.takima.backskeleton.services.debate.DebateSnapshot.Speaker;
import com.takima.backskeleton.services.debate.DebateSnapshot.StepState;
import com.takima.backskeleton.services.debate.DebateSnapshot.Turn;
import com.takima.backskeleton.services.llm.LlmException;
import com.takima.backskeleton.services.llm.LlmResult;
import com.takima.backskeleton.services.llm.LlmRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DebateRunnerTest {
    private static final Integer SESSION = 1;
    private static final Speaker PRUDENT = new Speaker(2, "Le Prudent", "Prudent", "p", "m", 0.5f, 600);
    private static final Speaker FINANCIER = new Speaker(4, "Le Financier", "Financier", "p", "m", 0.5f, 600);
    private static final Speaker SCRIBE = new Speaker(7, "Le Greffier", "Greffier", "p", "m", 0.2f, 2000);
    private static final String SYNTHESIS = "{\"verdict\":\"Lyon\",\"theme\":\"Emploi\"}";

    private final DebateStore store = mock(DebateStore.class);
    private final LlmRouter llm = mock(LlmRouter.class);
    private final DebateEventHub hub = mock(DebateEventHub.class);
    private final ScribeWatcher scribeWatcher = mock(ScribeWatcher.class);
    private DebateRunner runner;

    @BeforeEach
    void setUp() {
        runner = new DebateRunner(store, new DebatePrompts(), llm, new SynthesisParser(new ObjectMapper()),
                hub, scribeWatcher, new SyncTaskExecutor());
        when(store.append(anyInt(), anyInt(), any(), any(), any(), any())).thenReturn(mock(DebateDtos.MessageView.class));
    }

    private DebateSnapshot debate(SessionStatus status, StepState... steps) {
        return new DebateSnapshot(SESSION, "Camille", "Lyon ou Paris ?", null, status,
                List.of(PRUDENT, FINANCIER), SCRIBE, List.of(steps));
    }

    private StepState round(int id, StepStatus status, Turn... turns) {
        return new StepState(id, id, "Tour " + id, "Consigne", status, List.of(turns));
    }

    private StepState synthesisStep() {
        return new StepState(9, DebateScript.SYNTHESIS_ORDER, "Synthèse", "Consigne", StepStatus.PENDING, List.of());
    }

    @Test
    void resumesWithTheFirstSageWhoHasNotSpoken() {
        Turn alreadySaid = new Turn(50, PRUDENT.id(), PRUDENT.name(), "Déjà dit.");
        when(store.load(SESSION)).thenReturn(
                debate(SessionStatus.IN_PROGRESS, round(1, StepStatus.IN_PROGRESS, alreadySaid), synthesisStep()));
        when(llm.generate(any(), any()))
                .thenReturn(new LlmResult("Le Prudent oublie le coût.", "m", 10))
                .thenReturn(new LlmResult(SYNTHESIS, "m", 20));

        runner.ensureRunning(SESSION);

        verify(store, never()).append(eq(1), eq(PRUDENT.id()), any(), any(), any(), any());
        // Le Financier cite le Prudent : son message est rattaché à celui du Prudent.
        verify(store).append(1, FINANCIER.id(), "Le Prudent oublie le coût.", "m", 10, 50);
        verify(store).append(9, SCRIBE.id(), SYNTHESIS, "m", 20, null);
        verify(store).finish(SESSION, "Emploi");
        verify(hub).publish(SESSION, "status", Map.of("status", SessionStatus.FINISHED));
        verify(hub).close(SESSION);
    }

    @Test
    void aProviderFailureStopsTheDebateWithoutFinishingIt() {
        when(store.load(SESSION)).thenReturn(debate(SessionStatus.IN_PROGRESS, round(1, StepStatus.PENDING)));
        when(llm.generate(any(), any())).thenThrow(new LlmException("401", false));

        runner.ensureRunning(SESSION);

        verify(store, never()).finish(any(), any());
        verify(store, never()).append(any(), any(), any(), any(), any(), any());
        ArgumentCaptor<String> event = ArgumentCaptor.forClass(String.class);
        verify(hub, times(2)).publish(eq(SESSION), event.capture(), any());
        assertThat(event.getAllValues()).containsExactly("step", "failure");
        verify(hub).close(SESSION);
    }

    @Test
    void anUnreadableSynthesisIsRetriedThenFails() {
        when(store.load(SESSION)).thenReturn(debate(SessionStatus.IN_PROGRESS, synthesisStep()));
        when(llm.generate(any(), any())).thenReturn(new LlmResult("pas du json", "m", 1));

        runner.ensureRunning(SESSION);

        verify(llm, times(2)).generate(any(), any());
        verify(store, never()).finish(any(), any());
        verify(store, never()).append(any(), any(), any(), any(), any(), isNull());
    }

    @Test
    void everyTurnNotifiesTheScribeWithoutWaitingForIt() {
        when(store.load(SESSION)).thenReturn(debate(SessionStatus.IN_PROGRESS, round(1, StepStatus.PENDING)));
        when(llm.generate(any(), any())).thenReturn(new LlmResult("Un avis.", "m", 5));

        runner.ensureRunning(SESSION);

        verify(scribeWatcher, times(2)).onTurnSpoken(SESSION);
        verify(store, never()).append(any(), eq(SCRIBE.id()), any(), any(), any(), any());
    }

    @Test
    void aFinishedDebateOnlyAnnouncesItsStatus() {
        when(store.load(SESSION)).thenReturn(debate(SessionStatus.FINISHED));

        runner.ensureRunning(SESSION);

        verify(llm, never()).generate(any(), any());
        verify(hub).publish(SESSION, "status", Map.of("status", SessionStatus.FINISHED));
    }
}
