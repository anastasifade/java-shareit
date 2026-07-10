package ru.practicum.shareit.item.dto.item;

import lombok.Value;

@Value
public class UpdateItemDto {
    String name;
    String description;
    Boolean available;
}
