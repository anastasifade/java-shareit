package ru.practicum.shareit.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class UserMapper {

    public ResponseUserDto toDto(User user) {
        return ResponseUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public User toUser(UserDto dto) {
        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(dto.getEmail().trim());
        return user;
    }

    public User toUser(UserDto dto, User user) {
        if (dto.getName() != null) {
            user.setName(dto.getName().trim());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail().trim());
        }
        return user;
    }

}
