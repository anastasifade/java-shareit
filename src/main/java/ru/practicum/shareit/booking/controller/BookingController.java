package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.base.exceptions.InvalidQueryParameterException;
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
    public Collection<ResponseBookingDto> findAllAsBooker(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                          @RequestParam(defaultValue = "ALL") String state) {
        log.info("GET /bookings?state={} request received by BookingController. [X-Sharer-User-Id = {}]", state, userId);
        return findAll(userId, Role.BOOKER, state);
    }

    @GetMapping("/owner")
    public Collection<ResponseBookingDto> findAllAsOwner(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                          @RequestParam(defaultValue = "ALL") String state) {
        log.info("GET /bookings?state={}/owner request received by Booking Controller. [X-Sharer-User-Id = {}]",
                state, userId);
        return findAll(userId, Role.OWNER, state);
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
                                     @Valid @RequestBody NewBookingDto dto) {
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

    private Collection<ResponseBookingDto> findAll(long userId, Role userRole, String state) {
        try {
            BookingSearch filter = BookingSearch.valueOf(state);
            return service.findAll(userId, userRole, filter);
        } catch (IllegalArgumentException e) {
            log.warn("'{}' not a valid state value.", state);
            throw new InvalidQueryParameterException(String.format("'%s' is not a valid state value.", state));
        }
    }

}
