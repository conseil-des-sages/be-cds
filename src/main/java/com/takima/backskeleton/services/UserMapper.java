package com.takima.backskeleton.services;

import com.takima.backskeleton.DTO.UserDto;
import com.takima.backskeleton.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto mapToDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmailUser());
    }
}
