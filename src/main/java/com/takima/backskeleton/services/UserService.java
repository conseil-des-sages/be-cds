package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.UserDao;
import com.takima.backskeleton.DTO.RegisterDto;
import com.takima.backskeleton.DTO.UserDto;
import com.takima.backskeleton.models.Role;
import com.takima.backskeleton.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

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

    public UserDto create (RegisterDto register) {
        if(userDao.existByUserEmail(register.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un email existe déjà bouffon va, chien que tu es, VOLEUR !! AU VOLEUR !! UN ARABE VEUT ENTREE DANS NOTRE ABSE DE DONNEES !!");

        };
        User user = new User();
        user.setName(register.getName());
        user.setEmailUser(register.getEmail());
        user.setPasswordUser(PasswordEncoder.encode(register.getPassword()));
        user.setRole(new Role());
        return userMapper.mapToDto(userDao.save(user));

    }
}
