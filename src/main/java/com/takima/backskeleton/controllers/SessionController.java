package com.takima.backskeleton.controllers;

import com.takima.backskeleton.models.Session;
import com.takima.backskeleton.DTO.SessionDto;
import com.takima.backskeleton.services.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Session create(@RequestBody SessionDto dto) {
        return sessionService.create(dto);
    }

    @PutMapping("/{id}")
    public Session update(@PathVariable("id") Long id, @RequestBody SessionDto dto) {
        return sessionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        sessionService.delete(id);
    }
}
