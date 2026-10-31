package com.takima.backskeleton.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Qui siège à quelle session, et à quelle place : la place donne l'ordre de parole. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "participation")
public class Participation {
    @EmbeddedId
    private Key id;

    @MapsId("sessionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private Session session;

    @MapsId("participantId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @Column(name = "participation_seat")
    private Integer seat;

    @CreationTimestamp
    @Column(name = "participation_joined_at", updatable = false)
    private LocalDateTime joinedAt;

    public Participation(Session session, Participant participant, int seat) {
        this.id = new Key(session.getSessionId(), participant.getId());
        this.session = session;
        this.participant = participant;
        this.seat = seat;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        @Column(name = "session_id")
        private Integer sessionId;

        @Column(name = "participant_id")
        private Integer participantId;
    }
}
