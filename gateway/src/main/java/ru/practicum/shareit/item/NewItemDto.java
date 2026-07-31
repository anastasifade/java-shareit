package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class NewItemDto {
    @NotBlank(message = "Validation error: item name cannot be blank.")
    String name;
    @NotBlank(message = "Validation error: item description cannot be blank.")
    String description;
    @NotNull(message = "Validation error: item availability status must be specified.")
    Boolean available;
    Long requestId;
}
