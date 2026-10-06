package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.SessionDao;
import com.takima.backskeleton.DAO.StepDao;
import com.takima.backskeleton.DTO.StepDtos;
import com.takima.backskeleton.models.Session;
import com.takima.backskeleton.models.Step;
import com.takima.backskeleton.models.StepStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StepService {
    private final StepDao stepDao;
    private final SessionDao sessionDao;


    // =================================================================
    // LIRE
    // =================================================================

    public List<Step> findAll() {
        return stepDao.findAll();
    }

    public List<Step> findBySession(Integer sessionId) {
        return stepDao.findBySessionIdOrderByStepOrderAsc(sessionId);
    }

    public Step findById(Integer id) {
        return stepDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Étape " + id + " introuvable"));
    }

    // =================================================================
    // CRÉER
    // =================================================================

    @Transactional
    public Step create(StepDtos.CreateRequest request) {
        // 1. La session doit exister (sinon 404)
        Session session = sessionDao.findById(request.sessionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Session " + request.sessionId() + " introuvable"));

        // 2. Deux étapes d'une même session ne peuvent pas avoir le même ordre (sinon 409)
        //    La base le refuserait aussi (uq_step_order), mais avec une erreur SQL peu lisible.
        if (stepDao.existsBySessionIdAndStepOrder(request.sessionId(), request.stepOrder())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une étape d'ordre " + request.stepOrder() + " existe déjà dans cette session");
        }

        // 3. Création et enregistrement (le statut PENDING et les dates sont automatiques)
        Step step = new Step(session, request.stepOrder(), request.name(), request.description());
        return stepDao.save(step);
    }

    // =================================================================
    // MODIFIER
    // =================================================================

    @Transactional
    public Step update(Integer id, StepDtos.UpdateRequest request) {
        Step step = findById(id);   // réutilise la méthode du dessus : 404 si introuvable

        // Si l'ordre change, le nouvel ordre ne doit pas être déjà pris
        boolean orderChanged = !step.getOrder().equals(request.stepOrder());
        if (orderChanged && stepDao.existsBySessionIdAndStepOrder(
                step.getSession().getSessionId(), request.stepOrder())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une étape d'ordre " + request.stepOrder() + " existe déjà dans cette session");
        }

        step.setOrder(request.stepOrder());
        step.setName(request.name());
        step.setDescription(request.description());
        return stepDao.save(step);
    }

    @Transactional
    public Step updateStatus(Integer id, StepDtos.StatusUpdateRequest request) {
        Step step = findById(id);
        StepStatus current = step.getStatus();
        StepStatus next = request.status();

        // Règle 1 : on avance seulement dans l'ordre PENDING -> IN_PROGRESS -> DONE
        boolean allowed =
                (current == StepStatus.PENDING && next == StepStatus.IN_PROGRESS) ||
                        (current == StepStatus.IN_PROGRESS && next == StepStatus.DONE);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Passage de " + current + " à " + next + " interdit");
        }

        // Règle 2 : une seule étape en cours à la fois dans une session
        if (next == StepStatus.IN_PROGRESS && stepDao.existsBySessionIdAndStatus(
                step.getSession().getSessionId(), StepStatus.IN_PROGRESS)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une autre étape est déjà en cours dans cette session");
        }

        step.setStatus(next);
        return stepDao.save(step);
    }

    // =================================================================
    // SUPPRIMER
    // =================================================================

    @Transactional
    public void delete(Integer id) {
        Step step = findById(id);   // 404 si introuvable
        stepDao.delete(step);
    }
}
