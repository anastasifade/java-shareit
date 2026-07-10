package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResponseUserDto {
    Long id;
    String name;
    String email;
}
