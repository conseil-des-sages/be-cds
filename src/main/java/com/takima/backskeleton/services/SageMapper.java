package com.takima.backskeleton.services;

import com.takima.backskeleton.DTO.SageDto;
import com.takima.backskeleton.models.Sage;
import org.springframework.stereotype.Component;

@Component
public class SageMapper {
    public SageDto mapToDto(Sage sage) {
        return new SageDto(sage.getId(), sage.getName(), sage.getSageModel(), sage.getSageTemperature(), sage.getSageMaxTokens(), sage.getSageSystemPrompt(), sage.getSageDescription());
    }
}
