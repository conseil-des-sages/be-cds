package com.takima.backskeleton.controllers;

import com.takima.backskeleton.DTO.SageDtos;
import com.takima.backskeleton.services.SageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sages")
@RequiredArgsConstructor
public class SageController {
    private final SageService sageService;

    @GetMapping
    public List<SageDtos.View> findVisible(@AuthenticationPrincipal Integer userId) {
        return sageService.findVisible(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SageDtos.View create(@AuthenticationPrincipal Integer userId,
                                @Valid @RequestBody SageDtos.UpsertRequest request) {
        return sageService.create(userId, request);
    }

    @PutMapping("/{id}")
    public SageDtos.View update(@AuthenticationPrincipal Integer userId, @PathVariable Integer id,
                                @Valid @RequestBody SageDtos.UpsertRequest request) {
        return sageService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Integer userId, @PathVariable Integer id) {
        sageService.delete(userId, id);
    }
}
