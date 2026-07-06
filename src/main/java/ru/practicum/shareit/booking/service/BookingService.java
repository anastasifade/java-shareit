package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.BookingSearch;
import ru.practicum.shareit.base.auth.Role;

import java.util.Collection;

public interface BookingService {

    Collection<ResponseBookingDto> findAll(long userId, Role userRole, BookingSearch filter);

    ResponseBookingDto findById(long userId, long bookingId);

    ResponseBookingDto create(long userId, NewBookingDto request);

    ResponseBookingDto updateStatus(long userId, long bookingId, boolean approved);
}
