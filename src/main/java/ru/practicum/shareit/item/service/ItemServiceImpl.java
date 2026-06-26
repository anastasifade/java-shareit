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
        log.debug("Request for items [owner id={}] received by ItemService.", userId);
        return itemStorage.findByOwner(userId)
                .stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public Collection<ResponseItemDto> search(long userId, String text) {
        validateUser(userId);
        log.debug("Search request for items [search text = '{}'] received by ItemService.", text);
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
        log.debug("Request for item [id={}] received by ItemService.", itemId);
        Item item = itemStorage.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item with id=[%s] not found.", itemId)));
        return ItemMapper.toDto(item);
    }

    @Override
    public ResponseItemDto create(long userId, NewItemDto dto) {
        User owner = validateUser(userId);
        log.debug("Create request for an item, owner [id={}] received by ItemService.", userId);
        log.trace("Creating item: {}.", dto);
        Item item = ItemMapper.toItem(dto, owner);
        return ItemMapper.toDto(itemStorage.create(item));
    }

    @Override
    public ResponseItemDto update(long userId, long itemId, UpdateItemDto dto) {
        validateUser(userId);
        log.debug("Request to update item [id={}] by user [id={}] received by ItemService.", itemId, userId);
        Item item = itemStorage.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item with id=[%s] not found.", itemId)));

        if (item.getOwner().getId() != userId) {
            log.debug("Update failed: user [id={}] is not the owner of the item [id={}].", userId, itemId);
            throw new UserValidationException(String.format("Only item owner can edit item information."));
        }

        log.trace("Updating item: {}.", dto);
        item = ItemMapper.toItem(dto, item);
        return ItemMapper.toDto(itemStorage.update(item));
    }

    @Override
    public void delete(long userId, long itemId) {
        validateUser(userId); // validates user
        log.debug("Delete request for item [id={}] by user [id={}] received by ItemService.", itemId, userId);
        Item item = itemStorage.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item with id=[%s] not found.", itemId)));
        if (item.getOwner().getId() != userId) {
            log.debug("Item not deleted: user [id={}] not the item's owner.", userId);
            throw new UserValidationException("Only item's owner can remove the item.");
        }
        itemStorage.delete(itemId);
    }

    private User validateUser(long id) {
        return userStorage.findById(id).orElseThrow(() ->
                new UserValidationException(String.format("Request validation failed: user with id=[%s] not found.",
                        id)));
    }
}
