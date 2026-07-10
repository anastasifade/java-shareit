package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    Collection<ResponseUserDto> findAll();

    ResponseUserDto findById(long id);

    User getUser(long id);

    ResponseUserDto create(UserDto dto);

    ResponseUserDto update(long id, UserDto dto);

    void delete(long id);

    void validateUser(long id);
}
