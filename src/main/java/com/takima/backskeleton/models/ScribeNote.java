package com.takima.backskeleton.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Une remarque du Greffier, à côté du débat : elle n'appartient à aucun tour. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "scribe_note")
public class ScribeNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Integer id;

    @Column(name = "note_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "note_after_turns")
    private Integer afterTurns;

    @Column(name = "note_model")
    private String model;

    @CreationTimestamp
    @Column(name = "note_created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private Session session;

    public ScribeNote(Session session, int afterTurns, String content, String model) {
        this.session = session;
        this.afterTurns = afterTurns;
        this.content = content;
        this.model = model;
    }
}
