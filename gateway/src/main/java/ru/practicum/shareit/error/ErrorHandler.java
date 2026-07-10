package ru.practicum.shareit.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestControllerAdvice("ru.practicum.shareit")
public final class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final MethodArgumentNotValidException e) {
        List<String> errors = new ArrayList<>();
        e.getBindingResult()
                .getAllErrors()
                .forEach(error -> errors.add(error.getDefaultMessage()));
        return new ErrorResponse(errors.toString());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final InvalidQueryParameterException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handle(final Throwable e) {
        log.error("Internal server error: {}.", e.getMessage());
        log.debug("Error details: {}.", Arrays.toString(e.getStackTrace()));
        return new ErrorResponse(e.getMessage());
    }

}
