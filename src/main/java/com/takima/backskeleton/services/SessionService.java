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

    public List<Session> findAll() {
        return sessionDao.findAll();
    }

    public Session getById(Long id) {
        return sessionDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));
    }

    @Transactional
    public Session create(SessionDto dto) {
        validateName(dto);
        Session session = new Session();
        session.setSessionName(dto.getSessionName().trim());
        if (dto.getSessionStatus() != null) {
            session.setSessionStatus(dto.getSessionStatus());
        }
        session.setSessionCreatedAt(LocalDateTime.now());
        return sessionDao.save(session);
    }

    @Transactional
    public Session update(Long id, SessionDto dto) {
        Session session = getById(id);
        validateName(dto);
        session.setSessionName(dto.getSessionName().trim());
        if (dto.getSessionStatus() != null) {
            session.setSessionStatus(dto.getSessionStatus());
        }
        session.setSessionUpdatedAt(LocalDateTime.now());
        return sessionDao.save(session);
    }

    @Transactional
    public void delete(Long id) {
        sessionDao.delete(getById(id));
    }

    private void validateName(SessionDto dto) {
        if (dto.getSessionName() == null || dto.getSessionName().isBlank()
                || dto.getSessionName().trim().length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le nom de la session doit contenir entre 1 et 150 caracteres");
        }
    }
}
