package ru.practicum.shareit.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.shareit.validation.NullOrNotBlank;

public final class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, String> {
    @Override
    public void initialize(NullOrNotBlank constraint) {
    }

    @Override
    public boolean isValid(String field, ConstraintValidatorContext cxt) {
        return (field == null || !field.isBlank());
    }
}