package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    Collection<ResponseUserDto> findAll();

    ResponseUserDto findById(long id);

    User getUser(long id);

    ResponseUserDto create(NewUserDto dto);

    ResponseUserDto update(long id, UpdateUserDto dto);

    void delete(long id);

    void validateUser(long id);
}
