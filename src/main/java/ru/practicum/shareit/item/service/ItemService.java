package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.Collection;

public interface ItemService {
    Collection<ResponseItemDto> findByOwner(long ownerId);

    Collection<ResponseItemDto> search(long userId, String text);

    ResponseItemDto findById(long userid, long itemId);

    ResponseItemDto create(long userId, NewItemDto dto);

    ResponseItemDto update(long userId, long itemId, UpdateItemDto dto);

    void delete(long userId, long itemId);
}
