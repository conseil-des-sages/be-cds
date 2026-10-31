package com.takima.backskeleton.services.debate;

import com.takima.backskeleton.DTO.DebateDtos;
import com.takima.backskeleton.models.Message;
import com.takima.backskeleton.models.ScribeNote;
import com.takima.backskeleton.models.User;
import org.springframework.stereotype.Component;

@Component
public class DebateMapper {
    public DebateDtos.MessageView toView(Message message) {
        return new DebateDtos.MessageView(
                message.getId(),
                message.getStep().getId(),
                message.getParticipant().getId(),
                message.getParticipant().getName(),
                message.getParticipant() instanceof User,
                message.getContent(),
                message.getModel(),
                message.getParent() == null ? null : message.getParent().getParticipant().getId(),
                message.getCreatedAt());
    }

    public DebateDtos.NoteView toView(ScribeNote note) {
        return new DebateDtos.NoteView(note.getId(), note.getAfterTurns(), note.getContent(), note.getCreatedAt());
    }
}
