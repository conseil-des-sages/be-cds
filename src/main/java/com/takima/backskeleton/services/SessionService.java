package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.SessionDao;
import com.takima.backskeleton.models.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
}
