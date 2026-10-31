package com.takima.backskeleton.services.debate;

import com.takima.backskeleton.models.SessionStatus;
import com.takima.backskeleton.models.StepStatus;

import java.util.List;

/** L'état d'un débat, détaché de JPA : le moteur appelle les fournisseurs sans garder de transaction ouverte. */
public record DebateSnapshot(
        Integer sessionId,
        String ownerName,
        String question,
        String context,
        SessionStatus status,
        List<Speaker> sages,
        Speaker scribe,
        List<StepState> steps
) {
    public record Speaker(
            Integer id,
            String name,
            String shortName,
            String prompt,
            String model,
            float temperature,
            int maxTokens
    ) {
    }

    public record StepState(
            Integer id,
            int order,
            String name,
            String description,
            StepStatus status,
            List<Turn> turns
    ) {
        public boolean hasSpoken(Integer participantId) {
            return turns.stream().anyMatch(turn -> turn.participantId().equals(participantId));
        }
    }

    public record Turn(Integer messageId, Integer participantId, String authorName, String content) {
    }
}
