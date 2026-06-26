package ru.practicum.shareit.base.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.shareit.booking.dto.NewBookingDto;

public class BookingDateValidator implements ConstraintValidator<ValidBookingDate, NewBookingDto> {
    @Override
    public void initialize(ValidBookingDate constraint) {
    }

    @Override
    public boolean isValid(NewBookingDto dto, ConstraintValidatorContext cxt) {
        return dto.getEnd().isAfter(dto.getStart());
    }
}
