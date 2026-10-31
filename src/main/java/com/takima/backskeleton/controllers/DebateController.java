package com.takima.backskeleton.controllers;

import com.takima.backskeleton.DTO.DebateDtos;
import com.takima.backskeleton.services.debate.DebateEventHub;
import com.takima.backskeleton.services.debate.DebateRunner;
import com.takima.backskeleton.services.debate.DebateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/debates")
@RequiredArgsConstructor
public class DebateController {
    private final DebateService debateService;
    private final DebateRunner debateRunner;
    private final DebateEventHub eventHub;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DebateDtos.Detail create(@AuthenticationPrincipal Integer userId,
                                    @Valid @RequestBody DebateDtos.CreateRequest request) {
        return debateService.create(userId, request);
    }

    @GetMapping
    public List<DebateDtos.Summary> list(@AuthenticationPrincipal Integer userId) {
        return debateService.list(userId);
    }

    @GetMapping("/{id}")
    public DebateDtos.Detail get(@AuthenticationPrincipal Integer userId, @PathVariable Integer id) {
        return debateService.get(userId, id);
    }

    /** Suit le débat en direct, et le lance ou le reprend s'il n'est pas terminé. */
    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@AuthenticationPrincipal Integer userId, @PathVariable Integer id) {
        debateService.assertOwned(userId, id);
        SseEmitter emitter = eventHub.subscribe(id);
        debateRunner.ensureRunning(id);
        return emitter;
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public DebateDtos.MessageView interject(@AuthenticationPrincipal Integer userId, @PathVariable Integer id,
                                            @Valid @RequestBody DebateDtos.InterjectRequest request) {
        return debateService.interject(userId, id, request);
    }

    @PatchMapping("/{id}/archive")
    public DebateDtos.Summary archive(@AuthenticationPrincipal Integer userId, @PathVariable Integer id,
                                      @RequestBody DebateDtos.ArchiveRequest request) {
        return debateService.archive(userId, id, request.archived());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Integer userId, @PathVariable Integer id) {
        debateService.delete(userId, id);
    }
}
