package com.takima.backskeleton.DTO;

import com.takima.backskeleton.DTO.StepDtos.Response;
import com.takima.backskeleton.DTO.StepDtos.SummaryResponse;
import com.takima.backskeleton.models.Step;
import org.springframework.stereotype.Component;

/**
 * Convertit une entité Step en DTO de réponse.
 * Les DTO de requête sont lus directement par le service.
 */
@Component
public class StepMapper {

    // Pour le détail d'une étape (GET /{id}, POST, PUT, PATCH)
    public Response toResponse(Step step) {
        return new Response(
                step.getId(),
                step.getSession().getSessionId(),
                step.getOrder(),
                step.getName(),
                step.getDescription(),
                step.getStatus(),
                step.getCreatedAt(),
                step.getUpdatedAt()
        );
    }

    // Pour les listes (GET /api/steps?sessionId=3)
    public SummaryResponse toSummary(Step step) {
        return new SummaryResponse(
                step.getId(),
                step.getOrder(),
                step.getName(),
                step.getStatus()
        );
    }
}