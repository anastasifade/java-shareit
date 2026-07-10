package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public final class UserController {

    private final UserClient client;

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("GET /users request received by UserController.");
        return client.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable long id) {
        log.info("GET /users/{} request received by UserController.", id);
        return client.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@Valid @RequestBody NewUserDto user) {
        log.info("POST /users request received by UserController.");
        return client.create(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable long id, @Valid @RequestBody UpdateUserDto user) {
        log.info("PATCH /users/{} request received by UserController.", id);
        return client.update(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("DELETE /users/{} request received by UserController.", id);
        client.deleteUser(id);
    }

}
