package com.takima.backskeleton.DTO;

public class LoginRequest {
    private String email;
    private String mdp;

    public LoginRequest(String email, String mdp) {
        this.email = email;
        this.mdp = mdp;
    }
}
