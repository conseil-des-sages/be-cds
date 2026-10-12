package com.takima.backskeleton.controllers;

import com.takima.backskeleton.DTO.RoleDto;
import com.takima.backskeleton.models.Role;
import com.takima.backskeleton.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    public List<Role> findAll() {
        return roleService.findAll();
    }

    @GetMapping("/{id}")
    public Role getById(@PathVariable("id") Integer id) {
        return roleService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Role create(@RequestBody RoleDto dto) {
        return roleService.create(dto);
    }

    @PutMapping("/{id}")
    public Role update(@PathVariable("id") Integer id, @RequestBody RoleDto dto) {
        return roleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Integer id) {
        roleService.delete(id);
    }
}
