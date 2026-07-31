package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseRequestDto;

import java.util.Collection;

public interface RequestService {

    Collection<ResponseRequestDto> findAll(long userId);

    Collection<ResponseRequestDto> findAllByUser(long userId);

    ResponseRequestDto findById(long userId, long requestId);

    ResponseRequestDto create(long userId, ItemRequestDto dto);

}
