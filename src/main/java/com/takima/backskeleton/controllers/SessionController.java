package com.takima.backskeleton.controllers;

import com.takima.backskeleton.models.Session;
import com.takima.backskeleton.services.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    @GetMapping
    public List<Session> findAll() {
        return sessionService.findAll();
    }

    @GetMapping("/{id}")
    public Session getById(@PathVariable("id") Long id) {
        return sessionService.getById(id);
    }
}
