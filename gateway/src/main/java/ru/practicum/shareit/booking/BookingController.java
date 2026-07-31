package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.error.InvalidQueryParameterException;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public final class BookingController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private final BookingClient client;

    @GetMapping
    public ResponseEntity<Object> findAllAsBooker(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        log.info("GET /bookings?state={} request received by BookingController. [X-Sharer-User-Id = {}]", state, userId);
        return findAll(userId, Role.BOOKER, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findAllAsOwner(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                 @RequestParam(defaultValue = "ALL") String state) {
        log.info("GET /bookings?state={}/owner request received by Booking Controller. [X-Sharer-User-Id = {}]",
                state, userId);
        return findAll(userId, Role.OWNER, state);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                           @PathVariable("id") long bookingId) {
        log.info("GET /bookings/{} request received by BookingController. [X-Sharer-User-Id = {}]", bookingId, userId);
        return client.findById(userId, bookingId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                         @Valid @RequestBody NewBookingDto dto) {
        log.info("POST /bookings request received by BookingController. [X-Sharer-User-Id = {}]", userId);
        return client.create(userId, dto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateStatus(@RequestHeader(X_SHARER_USER_ID) long userId,
                                               @PathVariable("id") long bookingId,
                                               @RequestParam(defaultValue = "true") boolean approved) {
        log.info("PATCH /bookings/{}?approved={} request received by BookingController. [X-Sharer-User-Id = {}]",
                bookingId, approved, userId);
        return client.updateStatus(userId, bookingId, approved);
    }

    private ResponseEntity<Object> findAll(long userId, Role userRole, String state) {
        try {
            BookingSearch filter = BookingSearch.valueOf(state);
            return client.findAll(userId, userRole, filter);
        } catch (IllegalArgumentException e) {
            log.warn("'{}' not a valid state value.", state);
            throw new InvalidQueryParameterException(String.format("'%s' is not a valid state value.", state));
        }
    }

}
