package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.item.ItemNameDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;

import java.time.LocalDateTime;

@Value
@Builder
public class ResponseBookingDto {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    ItemNameDto item;
    ResponseUserDto booker;
    BookingStatus status;
}
