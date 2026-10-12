package com.takima.backskeleton.DTO;

import com.takima.backskeleton.models.StepStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Tous les DTO de Step, regroupés dans un seul fichier.
 *
 * Utilisation : StepDtos.CreateRequest, StepDtos.Response...
 * (ou "import com.takima.backskeleton.DTO.StepDtos.*;" pour écrire juste CreateRequest)
 *
 * La classe est final avec un constructeur privé : elle ne sert que de
 * "boîte de rangement", on ne crée jamais d'objet StepDtos.
 */
public final class StepDtos {

    private StepDtos() {
    }

    // =================================================================
    // REQUÊTES (ce qu'Angular envoie)
    // =================================================================

    /**
     * POST /api/steps
     * { "sessionId": 3, "stepOrder": 1, "name": "Ouverture", "description": "..." }
     */
    public record CreateRequest(
            @NotNull(message = "La session est obligatoire")
            Long sessionId,

            @NotNull(message = "L'ordre est obligatoire")
            @Min(value = 1, message = "L'ordre commence à 1")
            Integer stepOrder,

            @NotBlank(message = "Le nom est obligatoire")
            @Size(max = 100, message = "Le nom fait 100 caractères maximum")
            String name,

            @NotBlank(message = "La consigne est obligatoire")
            @Size(max = 500, message = "La consigne fait 500 caractères maximum")
            String description
    ) {
    }

    /**
     * PUT /api/steps/{id}
     * { "stepOrder": 2, "name": "Confrontation", "description": "..." }
     * Pas de sessionId : une étape ne change jamais de session.
     */
    public record UpdateRequest(
            @NotNull(message = "L'ordre est obligatoire")
            @Min(value = 1, message = "L'ordre commence à 1")
            Integer stepOrder,

            @NotBlank(message = "Le nom est obligatoire")
            @Size(max = 100, message = "Le nom fait 100 caractères maximum")
            String name,

            @NotBlank(message = "La consigne est obligatoire")
            @Size(max = 500, message = "La consigne fait 500 caractères maximum")
            String description
    ) {
    }

    /**
     * PATCH /api/steps/{id}/status
     * { "status": "IN_PROGRESS" }
     */
    public record StatusUpdateRequest(
            @NotNull(message = "Le statut est obligatoire")
            StepStatus status
    ) {
    }

    // =================================================================
    // RÉPONSES (ce que le back renvoie)
    // =================================================================

    /**
     * Réponse complète : GET /api/steps/{id}, POST, PUT, PATCH
     */
    public record Response(
            Long id,
            Long sessionId,
            Integer stepOrder,
            String name,
            String description,
            StepStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    /**
     * Réponse allégée pour les listes : GET /api/steps?sessionId=3
     */
    public record SummaryResponse(
            Long id,
            Integer stepOrder,
            String name,
            StepStatus status
    ) {
    }
}