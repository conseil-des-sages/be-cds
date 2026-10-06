package com.takima.backskeleton.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="\"user\"")
@PrimaryKeyJoinColumn(name = "participant_id")
public class User extends Participant{

    @Column(name = "user_email", unique = true)
    private String emailUser;

    @Column(name = "user_password")
    private String passwordUser;

    @Column(name = "user_last_login_at", nullable = true)
    private LocalDateTime lastLoginAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public String getPasswordUser() {
        return passwordUser;
    }

    public void setPasswordUser(String passwordUser) {
        this.passwordUser = passwordUser;
    }


    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
