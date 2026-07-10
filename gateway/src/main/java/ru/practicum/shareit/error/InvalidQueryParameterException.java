package ru.practicum.shareit.error;

public class InvalidQueryParameterException extends RuntimeException {
    public InvalidQueryParameterException(String message) {
        super(message);
    }
}
