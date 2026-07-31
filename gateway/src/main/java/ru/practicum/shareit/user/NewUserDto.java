package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class NewUserDto {
    @NotBlank(message = "Validation error: username cannot be blank.")
    String name;
    @NotNull(message = "Validation error: email cannot be blank.")
    @Email(message = "Validation error: invalid email format.")
    String email;
}
