package ru.practicum.shareit.base;

import java.util.Collection;
import java.util.Optional;

public interface Storage<T> {
    Collection<T> findAll();

    Optional<T> findById(long id);

    T create(T obj);

    T update(T obj);

    void delete(long id);
}
