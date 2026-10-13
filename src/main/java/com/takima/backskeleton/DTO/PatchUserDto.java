package com.takima.backskeleton.DTO;

import jakarta.validation.constraints.Email;

public class PatchUserDto {

    private String name;

    public PatchUserDto() {

    }

    public PatchUserDto(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
