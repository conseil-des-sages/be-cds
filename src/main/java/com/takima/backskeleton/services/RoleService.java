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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {
    private static final Set<String> REQUIRED_ROLES = Set.of("USER", "ADMIN");

    private final RoleDao roleDao;

    public List<RoleDto> findAll() {
        return roleDao.findAll().stream().map(this::toResponse).toList();
    }

    public RoleDto getById(Integer id) {
        return toResponse(findRoleById(id));
    }

    @Transactional
    public RoleDto create(RoleDto dto) {
        Role role = new Role();
        String name = validateName(dto);
        ensureNameAvailable(name, null);
        role.setRoleName(name);
        return toResponse(save(role));
    }

    @Transactional
    public RoleDto update(Integer id, RoleDto dto) {
        Role role = findRoleById(id);
        String newName = validateName(dto);
        if (REQUIRED_ROLES.contains(role.getRoleName()) && !role.getRoleName().equals(newName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Les rôles USER et ADMIN sont nécessaires au fonctionnement de l'application.");
        }
        ensureNameAvailable(newName, id);
        role.setRoleName(newName);
        return toResponse(save(role));
    }

    @Transactional
    public void delete(Integer id) {
        Role role = findRoleById(id);
        if (REQUIRED_ROLES.contains(role.getRoleName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Les rôles USER et ADMIN sont nécessaires au fonctionnement de l'application.");
        }
        try {
            roleDao.delete(role);
            roleDao.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce role est encore utilise", e);
        }
    }

    private Role findRoleById(Integer id) {
        return roleDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role introuvable"));
    }

    private RoleDto toResponse(Role role) {
        return new RoleDto(role.getRoleId(), role.getRoleName());
    }

    private void ensureNameAvailable(String name, Integer currentRoleId) {
        boolean alreadyExists = currentRoleId == null
                ? roleDao.existsByRoleName(name)
                : roleDao.existsByRoleNameAndRoleIdNot(name, currentRoleId);
        if (alreadyExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un rôle porte déjà ce nom.");
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
        if (dto == null || dto.getRoleName() == null || dto.getRoleName().isBlank()
                || dto.getRoleName().trim().length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le nom du role doit contenir entre 1 et 50 caracteres");
        }
        return dto.getRoleName().trim();
    }
}
