package ru.practicum.shareit.request;

import lombok.Builder;
import lombok.Value;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Value
@Builder
public final class ItemRequest {
    Long id;
    String description;
    User requestor;
    LocalDateTime created;
}
