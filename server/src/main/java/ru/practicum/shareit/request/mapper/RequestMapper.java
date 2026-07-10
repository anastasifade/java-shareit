package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class RequestMapper {

    public ItemRequest toRequest(ItemRequestDto dto, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription().trim());
        request.setCreated(LocalDateTime.now());
        request.setRequestor(requestor);
        return request;
    }

    public ResponseRequestDto toDto(ItemRequest request, List<Item> items) {
        return ResponseRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items.stream()
                        .map(ItemMapper::toBookingItemDto)
                        .toList())
                .build();
    }
}
