package ru.practicum.shareit.item.dto.item;

import lombok.Value;

@Value
public class NewItemDto {
    String name;
    String description;
    Boolean available;
    Long requestId;
}
