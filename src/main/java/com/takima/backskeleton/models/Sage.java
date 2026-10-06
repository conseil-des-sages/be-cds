package com.takima.backskeleton.models;

import jakarta.persistence.*;

@Entity
@Table(name = "sage")
@PrimaryKeyJoinColumn(name = "participant_id")
public class Sage extends Participant{

    @Column(name = "sage_model")
    private String sageModel;

    @Column(name = "sage_temperature")
    private Float sageTemperature;

    @Column(name = "sage_max_tokens")
    private Integer sageMaxTokens = 1000;

    @Column(name = "sage_system_prompt", columnDefinition = "TEXT")
    private String sageSystemPrompt;

    @Column(name = "sage_description")
    private String sageDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

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
        this.sageTemperature = sageTemperature;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}
