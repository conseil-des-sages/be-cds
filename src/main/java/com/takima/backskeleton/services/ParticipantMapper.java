package com.takima.backskeleton.services;

import com.takima.backskeleton.DTO.ParticipantDto;
import com.takima.backskeleton.models.Participant;
import org.springframework.stereotype.Component;

@Component
public class ParticipantMapper {

    public ParticipantDto toDto(Participant participant) {
        return new ParticipantDto(participant.getId(), participant.getName());
    }
}
