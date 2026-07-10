package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class RequestController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private final RequestClient client;

    @GetMapping
    public ResponseEntity<Object> findAllByUser(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("GET /requests by user [id = {}] received by Request Controller.", userId);
        return client.findAllByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("GET /requests/all by user [id = {}] received by Request Controller.", userId);
        return client.findAll(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                           @PathVariable long requestId) {
        log.info("GET /requests/{} request by user [id = {}] received by Request Controller.", requestId, userId);
        return client.findById(userId, requestId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                         @Valid @RequestBody ItemRequestDto dto) {
        log.info("POST /requests request by user [id = {}] received by Request Controller.", userId);
        return client.create(userId, dto);
    }

}
