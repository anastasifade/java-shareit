package ru.practicum.shareit.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.practicum.shareit.validation.ValidBookingDate;

import java.time.LocalDateTime;

@ValidBookingDate
@Value
public class NewBookingDto {
    @NotNull(message = "Item id cannot be null.")
    Long itemId;

    @NotNull(message = "Booking start date must be specified.")
    @FutureOrPresent(message = "Booking start date cannot be in the past.")
    LocalDateTime start;

    @NotNull(message = "Booking end date must be specified.")
    @Future(message = "Booking end date cannot be in the past.")
    LocalDateTime end;
}
