package ru.practicum.shareit.item.dal;

import ru.practicum.shareit.base.Storage;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemStorage extends Storage<Item> {
    Collection<Item> findByOwner(long ownerId);

    Collection<Item> search(String text);
}
