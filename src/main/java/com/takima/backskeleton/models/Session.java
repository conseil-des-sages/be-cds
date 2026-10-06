package com.takima.backskeleton.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "session")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;
    private String sessionName;
    @Enumerated(EnumType.STRING)
    private SessionStatus sessionStatus = SessionStatus.IN_PROGRESS;
    private LocalDateTime sessionCreatedAt;
    private LocalDateTime sessionUpdatedAt;
}
