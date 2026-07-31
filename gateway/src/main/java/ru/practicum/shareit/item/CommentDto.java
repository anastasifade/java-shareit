package ru.practicum.shareit.item;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class CommentDto {
    @NotBlank(message = "Comment text cannot be blank.")
    String text;
}
