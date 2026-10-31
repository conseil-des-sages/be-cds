package com.takima.backskeleton.services.debate;

import com.fasterxml.jackson.databind.JsonNode;
import com.takima.backskeleton.DAO.MessageDao;
import com.takima.backskeleton.DAO.ParticipationDao;
import com.takima.backskeleton.DAO.SageDao;
import com.takima.backskeleton.DAO.SessionDao;
import com.takima.backskeleton.DAO.StepDao;
import com.takima.backskeleton.DAO.UserDao;
import com.takima.backskeleton.DTO.DebateDtos;
import com.takima.backskeleton.DTO.SageDtos;
import com.takima.backskeleton.models.Message;
import com.takima.backskeleton.models.Participation;
import com.takima.backskeleton.models.Sage;
import com.takima.backskeleton.models.SageKind;
import com.takima.backskeleton.models.Session;
import com.takima.backskeleton.models.SessionStatus;
import com.takima.backskeleton.models.Step;
import com.takima.backskeleton.models.StepStatus;
import com.takima.backskeleton.models.User;
import com.takima.backskeleton.services.CouncilRules;
import com.takima.backskeleton.services.SageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebateService {
    private static final int TITLE_MAX = 150;

    private final SessionDao sessionDao;
    private final StepDao stepDao;
    private final MessageDao messageDao;
    private final ParticipationDao participationDao;
    private final UserDao userDao;
    private final SageDao sageDao;
    private final DebateStore store;
    private final DebateEventHub hub;
    private final DebateMapper debateMapper;
    private final SageMapper sageMapper;
    private final SynthesisParser synthesisParser;
    private final CouncilRules councilRules;

    @Value("${app.debate.daily-limit}")
    private int dailyLimit;

    @Transactional
    public DebateDtos.Detail create(Integer userId, DebateDtos.CreateRequest request) {
        User owner = userDao.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Compte introuvable."));
        long today = sessionDao.countByOwner_IdAndSessionCreatedAtAfter(userId, LocalDate.now().atStartOfDay());
        if (today >= dailyLimit) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Le conseil a beaucoup siégé aujourd'hui. Reviens demain.");
        }
        List<Sage> sages = councilRules.resolve(userId, request.sageIds());

        String question = request.question().trim();
        Session session = new Session();
        session.setSessionName(question.length() <= TITLE_MAX ? question : question.substring(0, TITLE_MAX));
        session.setSessionQuestion(question);
        session.setSessionContext(request.context() == null || request.context().isBlank() ? null : request.context().trim());
        session.setSessionCreatedAt(LocalDateTime.now());
        session.setOwner(owner);
        sessionDao.saveAndFlush(session);

        participationDao.save(new Participation(session, owner, 0));
        for (int seat = 0; seat < sages.size(); seat++) {
            participationDao.save(new Participation(session, sages.get(seat), seat + 1));
        }
        DebateScript.ROUNDS.forEach(round -> stepDao.save(
                new Step(session, round.order(), round.name(), round.description())));
        return toDetail(session);
    }

    @Transactional(readOnly = true)
    public List<DebateDtos.Summary> list(Integer userId) {
        return sessionDao.findByOwner_IdOrderBySessionCreatedAtDesc(userId).stream()
                .map(session -> new DebateDtos.Summary(
                        session.getSessionId(),
                        session.getSessionName(),
                        session.getSessionStatus(),
                        session.getSessionTheme(),
                        session.getSessionCreatedAt(),
                        sagesOf(session, userId)))
                .toList();
    }

    @Transactional(readOnly = true)
    public DebateDtos.Detail get(Integer userId, Integer debateId) {
        return toDetail(requireOwned(userId, debateId));
    }

    /** Vérifie l'accès avant d'ouvrir le flux d'événements. */
    @Transactional(readOnly = true)
    public void assertOwned(Integer userId, Integer debateId) {
        requireOwned(userId, debateId);
    }

    /** La personne ajoute une précision : le prochain sage à parler en tiendra compte. */
    public DebateDtos.MessageView interject(Integer userId, Integer debateId, DebateDtos.InterjectRequest request) {
        assertOwned(userId, debateId);
        DebateSnapshot debate = store.load(debateId);
        if (debate.status() != SessionStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce débat est terminé.");
        }
        Integer stepId = debate.steps().stream()
                .filter(step -> step.status() != StepStatus.DONE && step.order() != DebateScript.SYNTHESIS_ORDER)
                .findFirst()
                .map(DebateSnapshot.StepState::id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Le Greffier rédige la synthèse : le débat est clos."));
        DebateDtos.MessageView message = store.append(stepId, userId, request.content().trim(), null, null, null);
        hub.publish(debateId, "message", message);
        return message;
    }

    @Transactional
    public DebateDtos.Summary archive(Integer userId, Integer debateId, boolean archived) {
        Session session = requireOwned(userId, debateId);
        if (session.getSessionStatus() == SessionStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attends la fin du débat pour l'archiver.");
        }
        session.setSessionStatus(archived ? SessionStatus.ARCHIVED : SessionStatus.FINISHED);
        session.setSessionUpdatedAt(LocalDateTime.now());
        return new DebateDtos.Summary(session.getSessionId(), session.getSessionName(), session.getSessionStatus(),
                session.getSessionTheme(), session.getSessionCreatedAt(), sagesOf(session, userId));
    }

    @Transactional
    public void delete(Integer userId, Integer debateId) {
        sessionDao.delete(requireOwned(userId, debateId));
    }

    private DebateDtos.Detail toDetail(Session session) {
        Integer userId = session.getOwner().getId();
        Integer sessionId = session.getSessionId();
        List<Message> messages = messageDao.findBySession(sessionId);
        Map<Integer, List<Message>> messagesByStep = messages.stream()
                .collect(Collectors.groupingBy(message -> message.getStep().getId()));
        JsonNode synthesis = null;
        List<DebateDtos.StepView> steps = new java.util.ArrayList<>();
        for (Step step : stepDao.findBySession_SessionIdOrderByOrderAsc(sessionId)) {
            List<Message> stepMessages = messagesByStep.getOrDefault(step.getId(), List.of());
            if (step.getOrder() == DebateScript.SYNTHESIS_ORDER) {
                synthesis = stepMessages.stream().findFirst()
                        .flatMap(message -> synthesisParser.parse(message.getContent())).orElse(null);
                steps.add(new DebateDtos.StepView(step.getId(), step.getOrder(), step.getName(), step.getStatus(), List.of()));
            } else {
                steps.add(new DebateDtos.StepView(step.getId(), step.getOrder(), step.getName(), step.getStatus(),
                        stepMessages.stream().map(debateMapper::toView).toList()));
            }
        }
        return new DebateDtos.Detail(
                sessionId,
                session.getSessionQuestion(),
                session.getSessionContext(),
                session.getSessionStatus(),
                session.getSessionTheme(),
                session.getSessionCreatedAt(),
                sagesOf(session, userId),
                sageDao.findFirstByKindAndDeletedAtIsNull(SageKind.SCRIBE)
                        .map(scribe -> sageMapper.toViews(List.of(scribe), userId).get(0))
                        .orElse(null),
                steps,
                store.loadNotes(sessionId),
                synthesis);
    }

    private List<SageDtos.View> sagesOf(Session session, Integer userId) {
        List<Sage> sages = participationDao.findSages(session.getSessionId());
        return sageMapper.toViews(sages, userId);
    }

    /** 404 aussi pour le débat d'un autre joueur : on ne révèle pas son existence. */
    private Session requireOwned(Integer userId, Integer debateId) {
        return sessionDao.findById(debateId)
                .filter(session -> session.getOwner() != null && session.getOwner().getId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Débat introuvable."));
    }
}
