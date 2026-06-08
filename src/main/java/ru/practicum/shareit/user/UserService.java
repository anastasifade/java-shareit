package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.base.exceptions.DuplicateDataException;
import ru.practicum.shareit.base.exceptions.NotFoundException;
import ru.practicum.shareit.user.dal.UserStorage;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public final class UserService {

    private final UserStorage userStorage;

    public Collection<ResponseUserDto> findAll() {
        return userStorage.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    public ResponseUserDto findById(long id) {
        return UserMapper.toDto(getById(id));
    }

    public ResponseUserDto create(NewUserDto dto) {
        validateEmail(dto.getEmail());
        User user = userStorage.create(UserMapper.toUser(dto));
        return UserMapper.toDto(user);
    }

    public ResponseUserDto update(long id, UpdateUserDto dto) {
        User existingUser = getById(id);
        if (dto.getEmail() != null &&
                !dto.getEmail().isBlank() &&
                !dto.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            validateEmail(dto.getEmail());
        }
        User user = userStorage.update(UserMapper.toUser(dto, existingUser));
        return UserMapper.toDto(user);
    }

    public void delete(long id) {
        // validating user id
        getById(id);

        userStorage.delete(id);
    }

    private void validateEmail(String email) {
         if (userStorage.emailExists(email)) {
             throw new DuplicateDataException(String.format("Email [%s] already exists.", email));
         }
    }

    private User getById(long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=[%s] not found.", id)));
    }

}
