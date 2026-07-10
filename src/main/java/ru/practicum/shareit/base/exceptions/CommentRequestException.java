package ru.practicum.shareit.base.exceptions;

public class CommentRequestException extends RuntimeException {
    public CommentRequestException(String message) {
        super(message);
    }
}
