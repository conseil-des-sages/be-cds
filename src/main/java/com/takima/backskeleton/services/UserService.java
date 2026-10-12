package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.UserDao;
import com.takima.backskeleton.DTO.UserDto;
import com.takima.backskeleton.models.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserService {
    private UserDao userDao;
    private final UserMapper userMapper;

    public UserService(UserDao userDao, UserMapper userMapper){
        this.userDao = userDao;
        this.userMapper = userMapper;
    }

    public List<UserDto> findAll() {
        return userDao.findAll().stream()
                .map(userMapper::mapToDto)
                .toList();
    }
}
