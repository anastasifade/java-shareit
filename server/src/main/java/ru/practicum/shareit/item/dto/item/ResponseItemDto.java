package ru.practicum.shareit.item.dto.item;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.comment.ResponseCommentDto;

import java.util.Collection;

@Data
@Builder
public class ResponseItemDto {
    private final long id;
    private final String name;
    private final String description;
    private final boolean available;
    private final Collection<ResponseCommentDto> comments;
}
