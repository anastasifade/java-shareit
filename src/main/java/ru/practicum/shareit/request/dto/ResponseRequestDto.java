package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.item.ItemNameDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ResponseRequestDto {
    Long id;
    String description;
    LocalDateTime created;
    List<ItemNameDto> items;
}
