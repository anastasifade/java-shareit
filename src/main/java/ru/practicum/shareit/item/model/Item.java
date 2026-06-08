package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
@Builder
public final class Item {
    Long id;
    String name;
    String description;
    boolean available;
    User owner;
    ItemRequest request;
}
