package com.takima.backskeleton.DTO;

import com.takima.backskeleton.models.SessionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionDto {
    private String sessionName;
    private SessionStatus sessionStatus;
}
