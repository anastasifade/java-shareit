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
        log.debug("Request for list of all users received by UserService.");
        return userStorage.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    public ResponseUserDto findById(long id) {
        log.debug("Request for user [id={}] received by UserService.", id);
        return UserMapper.toDto(getById(id));
    }

    public ResponseUserDto create(NewUserDto dto) {
        validateEmail(dto.getEmail());
        log.debug("Request to create new user received by UserService.");
        log.trace("Creating user: [name={}, email={}].", dto.getName(), dto.getEmail());
        User user = userStorage.create(UserMapper.toUser(dto));
        return UserMapper.toDto(user);
    }

    public ResponseUserDto update(long id, UpdateUserDto dto) {
        log.debug("Request to update user received by UserService.");
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
        log.debug("Request to delete user [id={}] received by UserService.", id);
        userStorage.delete(id);
    }

    private void validateEmail(String email) {
         if (userStorage.emailExists(email)) {
             log.debug("Failed to validate email [{]]. Email already exists.", email);
             throw new DuplicateDataException(String.format("Email [%s] already exists.", email));
         }
    }

    private User getById(long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=[%s] not found.", id)));
    }

}
