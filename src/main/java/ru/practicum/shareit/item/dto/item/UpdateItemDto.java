package ru.practicum.shareit.item.dto.item;

import lombok.Value;
import ru.practicum.shareit.base.validation.NullOrNotBlank;

@Value
public class UpdateItemDto {
    @NullOrNotBlank(message = "Validation error: item name cannot be blank.")
    String name;
    @NullOrNotBlank(message = "Validation error: item description cannot be blank.")
    String description;
    Boolean available;
}
