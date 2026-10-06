package com.takima.backskeleton.DTO;

public class ParticipantDto {
    private Integer participantId;
    private String participantName;

    public Integer getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Integer participantId) {
        this.participantId = participantId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public ParticipantDto(Integer participantId, String participantName) {
        this.participantId = participantId;
        this.participantName = participantName;
    }


}
