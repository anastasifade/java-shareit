package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.base.exceptions.NotFoundException;
import ru.practicum.shareit.base.exceptions.UserValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dal.ItemStorage;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public final class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public Collection<ResponseItemDto> findByOwner(long userId) {
        validateUser(userId);
        return itemStorage.findByOwner(userId)
                .stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public Collection<ResponseItemDto> search(long userId, String text) {
        validateUser(userId);
        if (text.isBlank()) {
            return List.of();
        }
        return itemStorage.search(text)
                .stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public ResponseItemDto findById(long userid, long itemId) {
        validateUser(userid);
        Item item = itemStorage.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item with id=[%s] not found.", itemId)));
        return ItemMapper.toDto(item);
    }

    @Override
    public ResponseItemDto create(long userId, NewItemDto dto) {
        User owner = validateUser(userId);
        Item item = ItemMapper.toItem(dto, owner);
        return ItemMapper.toDto(itemStorage.create(item));
    }

    @Override
    public ResponseItemDto update(long userId, long itemId, UpdateItemDto dto) {
        validateUser(userId);
        Item item = itemStorage.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item with id=[%s] not found.", itemId)));

        if (item.getOwner().getId() != userId) {
            throw new UserValidationException(String.format("Only item owner can edit item information."));
        }

        item = ItemMapper.toItem(dto, item);
        return ItemMapper.toDto(itemStorage.update(item));
    }

    @Override
    public void delete(long userId, long itemId) {
        findById(userId, itemId); // validates user and item
        itemStorage.delete(itemId);
    }

    private User validateUser(long id) {
        return userStorage.findById(id).orElseThrow(() ->
                new UserValidationException(String.format("Request validation failed: user with id=[%s] not found.",
                        id)));
    }
}
