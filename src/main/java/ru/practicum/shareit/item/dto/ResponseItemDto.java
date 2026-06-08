package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import ru.practicum.shareit.validation.NullOrNotBlank;

@Value
@Builder
public class ResponseItemDto {
    @NotNull(message = "Validation error: item id cannot be null.")
    Long id;
    @NullOrNotBlank(message = "Validation error: item name cannot be blank.")
    String name;
    @NullOrNotBlank(message = "Validation error: item description cannot be blank.")
    String description;
    Boolean available;
}
