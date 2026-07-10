package ru.practicum.shareit.base.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public final class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, String> {
    @Override
    public void initialize(NullOrNotBlank constraint) {
    }

    @Override
    public boolean isValid(String field, ConstraintValidatorContext cxt) {
        return (field == null || !field.isBlank());
    }
}