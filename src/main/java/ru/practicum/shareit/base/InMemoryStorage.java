package ru.practicum.shareit.base;

import java.util.*;

public abstract class InMemoryStorage<T> implements Storage<T> {

    protected final Map<Long, T> storage = new HashMap<>();

    @Override
    public Collection<T> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public Optional<T> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    protected T insert(Long id, T obj) {
        storage.put(id, obj);
        return obj;
    }

    @Override
    public void delete(long id) {
        storage.remove(id);
    }

    protected Long getNextId() {
        long nextId = storage.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return ++nextId;
    }

}
