package com.takima.backskeleton.DTO;

public class UserDto extends ParticipantDto{
    private String userEmail;

    public UserDto(Integer participantId, String participantName ,String userEmail) {
        super(participantId, participantName);
        this.userEmail = userEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
