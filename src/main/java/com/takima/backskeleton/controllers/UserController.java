package com.takima.backskeleton.controllers;

import com.takima.backskeleton.DTO.PatchUserDto;
import com.takima.backskeleton.DTO.RegisterDto;
import com.takima.backskeleton.DTO.UserDto;
import com.takima.backskeleton.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RequestMapping("user")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody RegisterDto register) {
        return userService.create(register);
    }

    @PatchMapping("/{idUser}")
    public UserDto update(@PathVariable Integer idUser, @Valid @RequestBody PatchUserDto patchUserDto) {
        return userService.patch(idUser, patchUserDto);
    }

    @DeleteMapping("/{idUser}")
    public void delete(@PathVariable Integer idUser) {
        userService.delete(idUser);
    }
}
