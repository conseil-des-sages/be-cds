package com.takima.backskeleton.controllers;

import com.takima.backskeleton.DTO.SessionDto;
import com.takima.backskeleton.services.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    @GetMapping
    public List<SessionDto> findAll() {
        return sessionService.findAll();
    }

    @GetMapping("/{id}")
    public SessionDto getById(@PathVariable("id") Integer id) {
        return sessionService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionDto create(@Valid @RequestBody SessionDto dto) {
        return sessionService.create(dto);
    }

    @PutMapping("/{id}")
    public SessionDto update(@PathVariable("id") Integer id, @Valid @RequestBody SessionDto dto) {
        return sessionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Integer id) {
        sessionService.delete(id);
    }
}
