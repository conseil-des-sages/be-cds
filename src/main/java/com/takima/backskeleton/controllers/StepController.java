package com.takima.backskeleton.controllers;

import com.takima.backskeleton.DTO.StepDtos;
import com.takima.backskeleton.DTO.StepMapper;
import com.takima.backskeleton.models.Step;
import com.takima.backskeleton.services.StepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController                    // renvoie du JSON, pas des pages HTML
@RequestMapping("/api/steps")      // préfixe commun à toutes les URL de ce contrôleur
@RequiredArgsConstructor
public class StepController {
    private final StepService stepService;
    private final StepMapper stepMapper;

    // =================================================================
    // LIRE
    // =================================================================

    /**
     * GET /api/steps              -> toutes les étapes
     * GET /api/steps?sessionId=3  -> les étapes d'une session, dans l'ordre
     * Renvoie la version allégée (SummaryResponse), suffisante pour une liste.
     */
    @GetMapping
    public List<StepDtos.SummaryResponse> getAll(@RequestParam(required = false) Integer sessionId) {
        List<Step> steps = (sessionId == null)
                ? stepService.findAll()
                : stepService.findBySession(sessionId);
        return steps.stream().map(stepMapper::toSummary).toList();
    }

    /**
     * GET /api/steps/5 -> le détail complet d'une étape (404 si introuvable)
     */
    @GetMapping("/{id}")
    public StepDtos.Response getById(@PathVariable Integer id) {
        return stepMapper.toResponse(stepService.findById(id));
    }

    // =================================================================
    // CRÉER
    // =================================================================

    /**
     * POST /api/steps
     * Body : { "sessionId": 3, "stepOrder": 1, "name": "Ouverture", "description": "..." }
     * @Valid déclenche les vérifications du DTO (@NotBlank, @Size...) -> 400 si invalide
     * Renvoie 201 Created avec l'étape créée (et son id).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StepDtos.Response create(@Valid @RequestBody StepDtos.CreateRequest request) {
        return stepMapper.toResponse(stepService.create(request));
    }

    // =================================================================
    // MODIFIER
    // =================================================================

    /**
     * PUT /api/steps/5
     * Body : { "stepOrder": 2, "name": "Confrontation", "description": "..." }
     */
    @PutMapping("/{id}")
    public StepDtos.Response update(@PathVariable Integer id,
                                    @Valid @RequestBody StepDtos.UpdateRequest request) {
        return stepMapper.toResponse(stepService.update(id, request));
    }

    /**
     * PATCH /api/steps/5/status
     * Body : { "status": "IN_PROGRESS" }
     * PATCH (et pas PUT) car on ne modifie qu'une partie de l'étape.
     */
    @PatchMapping("/{id}/status")
    public StepDtos.Response updateStatus(@PathVariable Integer id,
                                          @Valid @RequestBody StepDtos.StatusUpdateRequest request) {
        return stepMapper.toResponse(stepService.updateStatus(id, request));
    }

    // =================================================================
    // SUPPRIMER
    // =================================================================

    /**
     * DELETE /api/steps/5 -> 204 No Content si supprimée, 404 si introuvable
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        stepService.delete(id);
    }
}
