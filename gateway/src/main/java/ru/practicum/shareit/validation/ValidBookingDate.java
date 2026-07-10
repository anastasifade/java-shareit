package ru.practicum.shareit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BookingDateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBookingDate {
    String message() default "Booking end date must be after booking start date.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
