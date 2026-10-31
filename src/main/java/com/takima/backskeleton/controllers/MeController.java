package com.takima.backskeleton.controllers;

import com.takima.backskeleton.DTO.MeDtos;
import com.takima.backskeleton.DTO.PatchUserDto;
import com.takima.backskeleton.services.MeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {
    private final MeService meService;

    @GetMapping
    public MeDtos.Profile profile(@AuthenticationPrincipal Integer userId) {
        return meService.profile(userId);
    }

    @PatchMapping
    public MeDtos.Profile rename(@AuthenticationPrincipal Integer userId, @Valid @RequestBody PatchUserDto request) {
        return meService.rename(userId, request);
    }

    @GetMapping("/council")
    public MeDtos.CouncilView council(@AuthenticationPrincipal Integer userId) {
        return meService.council(userId);
    }

    @PutMapping("/council")
    public MeDtos.CouncilView saveCouncil(@AuthenticationPrincipal Integer userId,
                                          @Valid @RequestBody MeDtos.CouncilRequest request) {
        return meService.saveCouncil(userId, request);
    }
}
