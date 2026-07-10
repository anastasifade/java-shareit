package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.BookingSearch;
import ru.practicum.shareit.base.auth.Role;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public final class BookingController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private final BookingService service;

    @GetMapping
    public Collection<ResponseBookingDto> findAll(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                  @RequestParam(defaultValue = "ALL") BookingSearch state,
                                                  @RequestParam() Role role) {
        log.info("GET /bookings?state={} request received by BookingController. [X-Sharer-User-Id = {}]",
                state, userId);
        return service.findAll(userId, role, state);
    }

    @GetMapping("/{id}")
    public ResponseBookingDto findById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                       @PathVariable("id") long bookingId) {
        log.info("GET /bookings/{} request received by BookingController. [X-Sharer-User-Id = {}]", bookingId, userId);
        return service.findById(userId, bookingId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseBookingDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                     @RequestBody NewBookingDto dto) {
        log.info("POST /bookings request received by BookingController. [X-Sharer-User-Id = {}]", userId);
        return service.create(userId, dto);
    }

    @PatchMapping("/{id}")
    public ResponseBookingDto updateStatus(@RequestHeader(X_SHARER_USER_ID) long userId,
                                           @PathVariable("id") long bookingId,
                                           @RequestParam boolean approved) {
        log.info("PATCH /bookings/{}?approved={} request received by BookingController. [X-Sharer-User-Id = {}]",
                bookingId, approved, userId);
        return service.updateStatus(userId, bookingId, approved);
    }

}
