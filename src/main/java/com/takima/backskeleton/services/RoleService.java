package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.RoleDao;
import com.takima.backskeleton.DTO.RoleDto;
import com.takima.backskeleton.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleDao roleDao;

    public List<Role> findAll() {
        return roleDao.findAll();
    }

    public Role getById(Long id) {
        return roleDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role introuvable"));
    }

    @Transactional
    public Role create(RoleDto dto) {
        Role role = new Role();
        role.setRoleName(validateName(dto));
        return save(role);
    }

    @Transactional
    public Role update(Long id, RoleDto dto) {
        Role role = getById(id);
        role.setRoleName(validateName(dto));
        return save(role);
    }

    @Transactional
    public void delete(Long id) {
        Role role = getById(id);
        try {
            roleDao.delete(role);
            roleDao.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce role est encore utilise", e);
        }
    }

    private Role save(Role role) {
        try {
            return roleDao.saveAndFlush(role);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le role ne peut pas etre enregistre : verifiez les contraintes de la base", e);
        }
    }

    private String validateName(RoleDto dto) {
        if (dto.getRoleName() == null || dto.getRoleName().isBlank()
                || dto.getRoleName().trim().length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le nom du role doit contenir entre 1 et 50 caracteres");
        }
        return dto.getRoleName().trim();
    }
}
