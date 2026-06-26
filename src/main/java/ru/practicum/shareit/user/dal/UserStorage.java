package ru.practicum.shareit.user.dal;

import ru.practicum.shareit.base.Storage;
import ru.practicum.shareit.user.model.User;

public interface UserStorage extends Storage<User> {
    boolean emailExists(String email);
}
