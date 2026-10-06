package com.takima.backskeleton.DTO;

public class SageDto extends ParticipantDto{
    private String sageModel;
    private Float sageTemperature;
    private Integer sageMaxTokens;
    private String sageSystemPrompt;
    private String sageDescription;

    public SageDto(Integer participantId, String participantName, String sageModel, Float sageTemperature, Integer sageMaxTokens, String sageSystemPrompt, String sageDescription) {
        super(participantId, participantName);
        this.sageModel = sageModel;
        this.sageTemperature = sageTemperature;
        this.sageMaxTokens = sageMaxTokens;
        this.sageSystemPrompt = sageSystemPrompt;
        this.sageDescription = sageDescription;
    }

    public String getSageModel() {
        return sageModel;
    }

    public void setSageModel(String sageModel) {
        this.sageModel = sageModel;
    }

    public Float getSageTemperature() {
        return sageTemperature;
    }

    public void setSageTemperature(Float sageTemperature) {
        sageTemperature = sageTemperature;
    }

    public Integer getSageMaxTokens() {
        return sageMaxTokens;
    }

    public void setSageMaxTokens(Integer sageMaxTokens) {
        this.sageMaxTokens = sageMaxTokens;
    }

    public String getSageSystemPrompt() {
        return sageSystemPrompt;
    }

    public void setSageSystemPrompt(String sageSystemPrompt) {
        this.sageSystemPrompt = sageSystemPrompt;
    }

    public String getSageDescription() {
        return sageDescription;
    }

    public void setSageDescription(String sageDescription) {
        this.sageDescription = sageDescription;
    }
}
