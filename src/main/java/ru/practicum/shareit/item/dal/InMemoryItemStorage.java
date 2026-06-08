package ru.practicum.shareit.item.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.base.InMemoryStorage;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@Slf4j
@Repository
public class InMemoryItemStorage extends InMemoryStorage<Item> implements ItemStorage {

    @Override
    public Collection<Item> findAll() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public Collection<Item> findByOwner(long ownerId) {
        return storage.values()
                .stream()
                .filter(item -> item.getOwner().getId() == ownerId)
                .toList();
    }

    @Override
    public Collection<Item> search(String text) {
        return storage.values()
                .stream()
                .filter(item -> item.isAvailable())
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .toList();
    }

    @Override
    public Item create(Item item) {
        item.setId(getNextId());
        return insert(item.getId(), item);
    }

    @Override
    public Item update(Item item) {
        return insert(item.getId(), item);
    }
}
