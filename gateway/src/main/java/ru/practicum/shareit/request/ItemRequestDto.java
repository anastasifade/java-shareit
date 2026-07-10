package ru.practicum.shareit.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ItemRequestDto {
    @NotBlank(message = "Validation error: item request description cannot be blank.")
    String description;
}
