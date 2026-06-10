package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public final class ItemMapper {

    public ResponseItemDto toDto(Item item) {
        return ResponseItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .build();
    }

    public Item toItem(NewItemDto dto, User owner) {
        return Item.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription().trim())
                .available(dto.getAvailable())
                .owner(owner)
                .build();
    }

    public Item toItem(UpdateItemDto dto, Item item) {
        return Item.builder()
                .id(item.getId())
                .name(dto.getName() == null ? item.getName() : dto.getName().trim())
                .description(dto.getDescription() == null ? item.getDescription() : dto.getDescription().trim())
                .available(dto.getAvailable() == null ? item.isAvailable() : dto.getAvailable())
                .owner(item.getOwner())
                .build();
    }
}
