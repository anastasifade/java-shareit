package ru.practicum.shareit.base.exceptions;

public class UserValidationException extends NotFoundException {
    public UserValidationException(String message) {
        super(message);
    }
}
