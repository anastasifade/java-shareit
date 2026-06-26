package ru.practicum.shareit.item.dto.item;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookingItemDto {
    long id;
    String name;
}
