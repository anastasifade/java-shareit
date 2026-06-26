package ru.practicum.shareit.item.dto.comment;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ResponseCommentDto {
    long id;
    String text;
    String authorName;
    LocalDateTime created;
}
