package com.takima.backskeleton.controllers;

import com.takima.backskeleton.DTO.LlmModelDto;
import com.takima.backskeleton.DTO.StatsDto;
import com.takima.backskeleton.services.StatsService;
import com.takima.backskeleton.services.llm.LlmRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogController {
    private final LlmRouter llmRouter;
    private final StatsService statsService;

    @GetMapping("/llm/models")
    public List<LlmModelDto> models() {
        return llmRouter.catalog();
    }

    @GetMapping("/stats")
    public StatsDto stats() {
        return statsService.stats();
    }
}
