package com.takima.backskeleton.services.debate;

import com.takima.backskeleton.DAO.MessageDao;
import com.takima.backskeleton.DAO.ParticipantDao;
import com.takima.backskeleton.DAO.ParticipationDao;
import com.takima.backskeleton.DAO.SageDao;
import com.takima.backskeleton.DAO.ScribeNoteDao;
import com.takima.backskeleton.DAO.SessionDao;
import com.takima.backskeleton.DAO.StepDao;
import com.takima.backskeleton.DTO.DebateDtos;
import com.takima.backskeleton.models.Message;
import com.takima.backskeleton.models.Participant;
import com.takima.backskeleton.models.Sage;
import com.takima.backskeleton.models.SageKind;
import com.takima.backskeleton.models.ScribeNote;
import com.takima.backskeleton.models.Session;
import com.takima.backskeleton.models.SessionStatus;
import com.takima.backskeleton.models.Step;
import com.takima.backskeleton.models.StepStatus;
import com.takima.backskeleton.services.debate.DebateSnapshot.Speaker;
import com.takima.backskeleton.services.debate.DebateSnapshot.StepState;
import com.takima.backskeleton.services.debate.DebateSnapshot.Turn;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Les lectures et écritures du moteur, chacune dans sa propre transaction courte. */
@Service
@RequiredArgsConstructor
public class DebateStore {
    private final SessionDao sessionDao;
    private final StepDao stepDao;
    private final MessageDao messageDao;
    private final ParticipantDao participantDao;
    private final ParticipationDao participationDao;
    private final SageDao sageDao;
    private final ScribeNoteDao scribeNoteDao;
    private final DebateMapper mapper;

    @Transactional(readOnly = true)
    public DebateSnapshot load(Integer sessionId) {
        Session session = sessionDao.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Débat introuvable."));
        Map<Integer, List<Turn>> turnsByStep = messageDao.findBySession(sessionId).stream()
                .collect(Collectors.groupingBy(
                        message -> message.getStep().getId(),
                        Collectors.mapping(this::toTurn, Collectors.toList())));
        List<StepState> steps = stepDao.findBySession_SessionIdOrderByOrderAsc(sessionId).stream()
                .map(step -> new StepState(step.getId(), step.getOrder(), step.getName(), step.getDescription(),
                        step.getStatus(), turnsByStep.getOrDefault(step.getId(), List.of())))
                .toList();
        Speaker scribe = sageDao.findFirstByKindAndDeletedAtIsNull(SageKind.SCRIBE).map(this::toSpeaker)
                .orElseThrow(() -> new IllegalStateException("Le Greffier n'existe pas en base."));
        return new DebateSnapshot(
                session.getSessionId(),
                session.getOwner() == null ? "La personne" : session.getOwner().getName(),
                session.getSessionQuestion(),
                session.getSessionContext(),
                session.getSessionStatus(),
                participationDao.findSages(sessionId).stream().map(this::toSpeaker).toList(),
                scribe,
                steps);
    }

    @Transactional
    public DebateDtos.MessageView append(Integer stepId, Integer participantId, String content,
                                         String model, Integer tokenCount, Integer parentId) {
        Step step = stepDao.findByIdForUpdate(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Étape introuvable."));
        Participant participant = participantDao.findById(participantId)
                .orElseThrow(() -> new IllegalStateException("Participant " + participantId + " introuvable."));
        Message message = new Message();
        message.setStep(step);
        message.setParticipant(participant);
        message.setContent(content);
        message.setModel(model);
        message.setTokenCount(tokenCount);
        message.setPosition(messageDao.findLastPosition(stepId) + 1);
        if (parentId != null) {
            message.setParent(messageDao.findById(parentId).orElse(null));
        }
        return mapper.toView(messageDao.save(message));
    }

    @Transactional(readOnly = true)
    public List<DebateDtos.NoteView> loadNotes(Integer sessionId) {
        return scribeNoteDao.findBySession_SessionIdOrderByAfterTurnsAsc(sessionId).stream()
                .map(mapper::toView)
                .toList();
    }

    @Transactional
    public DebateDtos.NoteView saveNote(Integer sessionId, int afterTurns, String content, String model) {
        Session session = sessionDao.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("Débat " + sessionId + " introuvable."));
        return mapper.toView(scribeNoteDao.save(new ScribeNote(session, afterTurns, content, model)));
    }

    @Transactional
    public void setStepStatus(Integer stepId, StepStatus status) {
        stepDao.findById(stepId).ifPresent(step -> step.setStatus(status));
    }

    @Transactional
    public void finish(Integer sessionId, String theme) {
        sessionDao.findById(sessionId).ifPresent(session -> {
            session.setSessionStatus(SessionStatus.FINISHED);
            session.setSessionUpdatedAt(LocalDateTime.now());
            session.setSessionTheme(theme);
        });
    }

    private Turn toTurn(Message message) {
        return new Turn(message.getId(), message.getParticipant().getId(),
                message.getParticipant().getName(), message.getContent());
    }

    private Speaker toSpeaker(Sage sage) {
        return new Speaker(sage.getId(), sage.getName(), sage.getShortName(), sage.getSageSystemPrompt(),
                sage.getSageModel(), sage.getSageTemperature(), sage.getSageMaxTokens());
    }
}
