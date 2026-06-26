package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.BookingSearch;
import ru.practicum.shareit.booking.model.BookingStatus;
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
                                                          @RequestParam(defaultValue = "ALL") BookingSearch state) {
        return service.findAll(userId, Role.BOOKER, state);
    }

    @GetMapping("/owner")
    public Collection<ResponseBookingDto> findAllAsOwner(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                          @RequestParam(defaultValue = "ALL") BookingSearch state) {
        return service.findAll(userId, Role.OWNER, state);
    }

    @GetMapping("/{id}")
    public ResponseBookingDto findById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                       @PathVariable("id") long bookingId) {
        return service.findById(userId, bookingId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseBookingDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                     @Valid @RequestBody NewBookingDto dto) {
        return service.create(userId, dto);
    }

    @PatchMapping("/{id}")
    public ResponseBookingDto updateStatus(@RequestHeader(X_SHARER_USER_ID) long userId,
                                           @PathVariable("id") long bookingId,
                                           @RequestParam boolean approved) {
        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        return service.updateStatus(userId, bookingId, status);
    }

}
