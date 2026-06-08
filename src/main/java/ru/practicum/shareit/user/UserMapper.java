package ru.practicum.shareit.user;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public final class UserMapper {

    public static ResponseUserDto toDto(User user) {
        return ResponseUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(NewUserDto dto) {
        return User.builder()
                .name(dto.getName().trim())
                .email(dto.getEmail().trim())
                .build();
    }

    public static User toUser(UpdateUserDto newUser, User oldUser) {
        return User.builder()
                .id(oldUser.getId())
                .name(newUser.getName() == null ? oldUser.getName() : newUser.getName().trim())
                .email(newUser.getEmail() == null ? oldUser.getEmail() : newUser.getEmail().trim())
                .build();
    }

}
