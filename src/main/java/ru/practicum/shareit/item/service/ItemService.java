package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.comment.CommentDto;
import ru.practicum.shareit.item.dto.comment.ResponseCommentDto;
import ru.practicum.shareit.item.dto.item.NewItemDto;
import ru.practicum.shareit.item.dto.item.ResponseItemDto;
import ru.practicum.shareit.item.dto.item.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {
    Collection<? extends ResponseItemDto> findByOwner(long ownerId);

    Collection<ResponseItemDto> search(long userId, String text);

    ResponseItemDto findById(long userid, long itemId);

    Item getItem(long itemId);

    ResponseItemDto create(long userId, NewItemDto dto);

    ResponseCommentDto createComment(long userId, long itemId, CommentDto dto);

    ResponseItemDto update(long userId, long itemId, UpdateItemDto dto);

    void delete(long userId, long itemId);
}
