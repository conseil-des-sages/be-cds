package com.takima.backskeleton.DTO;

import com.takima.backskeleton.models.SessionStatus;

import java.time.LocalDateTime;

public class SessionDto {
    private Integer sessionId;
    private String sessionName;
    private SessionStatus sessionStatus;
    private LocalDateTime sessionCreatedAt;
    private LocalDateTime sessionUpdatedAt;

    public SessionDto() {
    }

    public SessionDto(Integer sessionId, String sessionName, SessionStatus sessionStatus,
                      LocalDateTime sessionCreatedAt, LocalDateTime sessionUpdatedAt) {
        this.sessionId = sessionId;
        this.sessionName = sessionName;
        this.sessionStatus = sessionStatus;
        this.sessionCreatedAt = sessionCreatedAt;
        this.sessionUpdatedAt = sessionUpdatedAt;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
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

    public void setSessionCreatedAt(LocalDateTime sessionCreatedAt) {
        this.sessionCreatedAt = sessionCreatedAt;
    }

    public LocalDateTime getSessionUpdatedAt() {
        return sessionUpdatedAt;
    }

    public void setSessionUpdatedAt(LocalDateTime sessionUpdatedAt) {
        this.sessionUpdatedAt = sessionUpdatedAt;
    }
}
