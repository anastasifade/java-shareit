package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class User {
    Long id;
    String name;
    String email;
}
