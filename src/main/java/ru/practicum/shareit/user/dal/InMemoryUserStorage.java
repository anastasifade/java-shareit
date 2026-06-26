package ru.practicum.shareit.user.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.base.InMemoryStorage;
import ru.practicum.shareit.user.model.User;

@Slf4j
@Repository
public final class InMemoryUserStorage extends InMemoryStorage<User> implements UserStorage {

    @Override
    public boolean emailExists(String email) {
        log.trace("Email validation request for email [{}] receive by in-memory user storage.", email);
        return storage.values()
                .stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public User create(User user) {
        log.trace("Create request received by in-memory user storage, [user={}].", user);
        user.setId(getNextId());
        log.debug("User assigned id: {}.", user.getId());
        return insert(user.getId(), user);
    }

    @Override
    public User update(User user) {
        log.trace("Update request received by in-memory user storage, [user={}].", user);
        return insert(user.getId(), user);
    }

}
