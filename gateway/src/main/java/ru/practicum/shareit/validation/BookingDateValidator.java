package ru.practicum.shareit.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.shareit.validation.ValidBookingDate;
import ru.practicum.shareit.booking.NewBookingDto;

import java.time.LocalDateTime;

public class BookingDateValidator implements ConstraintValidator<ValidBookingDate, NewBookingDto> {
    @Override
    public void initialize(ValidBookingDate constraint) {
    }

    @Override
    public boolean isValid(NewBookingDto dto, ConstraintValidatorContext cxt) {
        if (dto.getStart() == null || dto.getEnd() == null) {
            return true; // triggers @NotNull constraint during field validation
        }
        if (dto.getEnd().isBefore(LocalDateTime.now())) {
            return true; // triggers @Future constraint during field validation
        }
        return dto.getEnd().isAfter(dto.getStart());
    }
}
