package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseRequestDto;
import ru.practicum.shareit.request.service.RequestService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class RequestController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private final RequestService service;

    @GetMapping
    public Collection<ResponseRequestDto> findAllByUser(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("GET /requests by user [id = {}] received by Request Controller.", userId);
        return service.findAllByUser(userId);
    }

    @GetMapping("/all")
    public Collection<ResponseRequestDto> findAll(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("GET /requests/all by user [id = {}] received by Request Controller.", userId);
        return service.findAll(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseRequestDto findById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                       @PathVariable long requestId) {
        log.info("GET /requests/{} request by user [id = {}] received by Request Controller.", requestId, userId);
        return service.findById(userId, requestId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseRequestDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                     @RequestBody ItemRequestDto dto) {
        log.info("POST /requests request by user [id = {}] received by Request Controller.", userId);
        return service.create(userId, dto);
    }

}
