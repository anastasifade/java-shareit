package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Value;
import ru.practicum.shareit.base.validation.NullOrNotBlank;

@Value
public class UpdateUserDto {
    @NullOrNotBlank(message = "Validation error: username cannot be blank.")
    String name;
    @NullOrNotBlank(message = "Validation error: email cannot be blank.")
    @Email(message = "Validation error: invalid email format.")
    String email;
}
