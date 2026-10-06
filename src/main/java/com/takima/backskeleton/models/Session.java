package com.takima.backskeleton.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "session")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;
    @Column(name = "session_name")
    private String sessionName;
    @Enumerated(EnumType.STRING)
    @Column(name = "session_status")
    private SessionStatus sessionStatus = SessionStatus.IN_PROGRESS;

    @Column(name = "session_created_at")
    private LocalDateTime sessionCreatedAt;

    @Column(name = "session_updated_at")
    private LocalDateTime sessionUpdatedAt;

    public Session() {
    }

    @PrePersist
    private void onCreate() {
        sessionCreatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        sessionUpdatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public LocalDateTime getSessionCreatedAt() {
        return sessionCreatedAt;
    }

    public LocalDateTime getSessionUpdatedAt() {
        return sessionUpdatedAt;
    }
}
