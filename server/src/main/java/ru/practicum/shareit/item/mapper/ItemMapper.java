package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.item.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@UtilityClass
public class ItemMapper {

    public ResponseItemDto toDto(Item item, List<Comment> comments) {
        return ResponseItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .comments(comments.stream()
                        .map(CommentMapper::toDto)
                        .toList())
                .build();
    }

    public OwnerItemDto toOwnerDto(Item item, Booking last, Booking next, List<Comment> comments) {
        return new OwnerItemDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                last == null ? null : BookingMapper.toDto(last),
                next == null ? null : BookingMapper.toDto(next),
                comments.stream()
                        .map(CommentMapper::toDto)
                        .toList());
    }

    public ItemNameDto toBookingItemDto(Item item) {
        return ItemNameDto.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }

    public Item toItem(NewItemDto dto, User owner) {
        Item item = new Item();
        item.setName(dto.getName().trim());
        item.setDescription(dto.getDescription().trim());
        item.setAvailable(dto.getAvailable());
        item.setOwner(owner);
        return item;
    }

    public Item toItem(UpdateItemDto dto, Item item) {
        if (dto.getName() != null) {
            item.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription().trim());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
        return item;
    }
}
