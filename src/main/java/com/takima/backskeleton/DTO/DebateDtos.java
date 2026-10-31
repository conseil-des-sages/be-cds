package com.takima.backskeleton.DTO;

import com.fasterxml.jackson.databind.JsonNode;
import com.takima.backskeleton.models.SessionStatus;
import com.takima.backskeleton.models.StepStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class DebateDtos {
    private DebateDtos() {
    }

    /** POST /api/debates */
    public record CreateRequest(
            @NotBlank(message = "Pose ta question au conseil.")
            @Size(min = 5, max = 400, message = "La question fait entre 5 et 400 caractères.")
            String question,
            @Size(max = 2000, message = "Le contexte fait 2000 caractères maximum.")
            String context,
            @NotEmpty(message = "Choisis les sages du conseil.")
            List<Integer> sageIds
    ) {
    }

    /** POST /api/debates/{id}/messages */
    public record InterjectRequest(
            @NotBlank(message = "Ton message est vide.")
            @Size(max = 1000, message = "Ton message fait 1000 caractères maximum.")
            String content
    ) {
    }

    /** PATCH /api/debates/{id}/archive */
    public record ArchiveRequest(boolean archived) {
    }

    public record Summary(
            Integer id,
            String title,
            SessionStatus status,
            String theme,
            LocalDateTime createdAt,
            List<SageDtos.View> sages
    ) {
    }

    public record Detail(
            Integer id,
            String question,
            String context,
            SessionStatus status,
            String theme,
            LocalDateTime createdAt,
            List<SageDtos.View> sages,
            SageDtos.View scribe,
            List<StepView> steps,
            List<NoteView> notes,
            JsonNode synthesis
    ) {
    }

    public record StepView(
            Integer id,
            Integer order,
            String name,
            StepStatus status,
            List<MessageView> messages
    ) {
    }

    /** Une remarque du Greffier : elle s'affiche une fois afterTurns interventions prononcées. */
    public record NoteView(Integer id, Integer afterTurns, String content, LocalDateTime createdAt) {
    }

    public record MessageView(
            Integer id,
            Integer stepId,
            Integer participantId,
            String authorName,
            boolean fromUser,
            String content,
            String model,
            Integer repliesToParticipantId,
            LocalDateTime createdAt
    ) {
    }
}
