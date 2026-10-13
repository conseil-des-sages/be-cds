package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.RoleDao;
import com.takima.backskeleton.DAO.UserDao;
import com.takima.backskeleton.DTO.PatchUserDto;
import com.takima.backskeleton.DTO.RegisterDto;
import com.takima.backskeleton.DTO.UserDto;
import com.takima.backskeleton.models.Role;
import com.takima.backskeleton.models.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {
    private final UserDao userDao;
    private final RoleDao roleDao;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDao userDao, UserMapper userMapper,
                       PasswordEncoder passwordEncoder, RoleDao roleDao) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDto> findAll() {
        return userDao.findAll().stream()
                .map(userMapper::mapToDto)
                .toList();
    }


    public User findByIdUser(@NotBlank Integer userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Utiliateur introuvouble"
                ));
    }


    @Transactional
    public UserDto create(RegisterDto register) {
        if (register == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le corps de la requête est obligatoire.");
        }
        String email = register.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userDao.existsByEmailUser(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette adresse e-mail est déjà utilisée.");
        }

        Role userRole = roleDao.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Le rôle USER n'existe pas en base."));

        User user = new User();
        user.setName(register.getName().trim());
        user.setEmailUser(email);
        user.setPasswordUser(passwordEncoder.encode(register.getPassword()));
        user.setRole(userRole);

        try {
            return userMapper.mapToDto(userDao.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "L'utilisateur entre en conflit avec les données existantes.", exception);
        }

        public UserDto patch (Integer idUser, PatchUserDto patchUserDto) {
            User user = findByIdUser(idUser);
            if (patchUserDto.getName() != null) {
                user.setName(patchUserDto.getName());
            }
            return userMapper.mapToDto(userDao.save(user));
        }

        public void delete (Integer idUser){
            User user = findByIdUser(idUser);
            userDao.delete(user);
        }

    }


}
