package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;

import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public final class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<ResponseUserDto> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseUserDto findById(@PathVariable long id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseUserDto create(@Valid @RequestBody NewUserDto user) {
        return userService.create(user);
    }

    @PatchMapping("/{id}")
    public ResponseUserDto update(@PathVariable long id, @Valid @RequestBody UpdateUserDto user) {
        return userService.update(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        userService.delete(id);
    }

}
