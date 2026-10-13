package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.SessionDao;
import com.takima.backskeleton.DTO.SessionDto;
import com.takima.backskeleton.models.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionDao sessionDao;

    public List<SessionDto> findAll() {
        return sessionDao.findAll().stream().map(this::toResponse).toList();
    }

    public SessionDto getById(Integer id) {
        return toResponse(findSessionById(id));
    }

    @Transactional
    public SessionDto create(SessionDto dto) {
        validateName(dto);
        Session session = new Session();
        session.setSessionName(dto.getSessionName().trim());
        if (dto.getSessionStatus() != null) {
            session.setSessionStatus(dto.getSessionStatus());
        }
        session.setSessionCreatedAt(LocalDateTime.now());
        return toResponse(sessionDao.save(session));
    }

    @Transactional
    public SessionDto update(Integer id, SessionDto dto) {
        Session session = findSessionById(id);
        validateName(dto);
        session.setSessionName(dto.getSessionName().trim());
        if (dto.getSessionStatus() != null) {
            session.setSessionStatus(dto.getSessionStatus());
        }
        session.setSessionUpdatedAt(LocalDateTime.now());
        return toResponse(sessionDao.save(session));
    }

    @Transactional
    public void delete(Integer id) {
        sessionDao.delete(findSessionById(id));
    }

    private Session findSessionById(Integer id) {
        return sessionDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));
    }

    private SessionDto toResponse(Session session) {
        return new SessionDto(
                session.getSessionId(),
                session.getSessionName(),
                session.getSessionStatus(),
                session.getSessionCreatedAt(),
                session.getSessionUpdatedAt());
    }

    private void validateName(SessionDto dto) {
        if (dto == null || dto.getSessionName() == null || dto.getSessionName().isBlank()
                || dto.getSessionName().trim().length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le nom de la session doit contenir entre 1 et 150 caracteres");
        }
    }
}
