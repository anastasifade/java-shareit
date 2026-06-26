package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public final class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<ResponseUserDto> findAll() {
        log.info("GET /users request received by UserController.");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseUserDto findById(@PathVariable long id) {
        log.info("GET /users/{} request received by UserController.", id);
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseUserDto create(@Valid @RequestBody NewUserDto user) {
        log.info("POST /users request received by UserController.");
        return userService.create(user);
    }

    @PatchMapping("/{id}")
    public ResponseUserDto update(@PathVariable long id, @Valid @RequestBody UpdateUserDto user) {
        log.info("PATCH /users/{} request received by UserController.", id);
        return userService.update(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("DELETE /users/{} request received by UserController.", id);
        userService.delete(id);
    }

}
