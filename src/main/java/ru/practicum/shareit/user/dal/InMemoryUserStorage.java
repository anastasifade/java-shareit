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
        return storage.values()
                .stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        return insert(user.getId(), user);
    }

    @Override
    public User update(User user) {
        return insert(user.getId(), user);
    }

}
